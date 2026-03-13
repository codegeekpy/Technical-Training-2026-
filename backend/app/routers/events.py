from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.event_proposal import EventProposal, ProposalStatus
from app.models.user import User, UserRole
from app.schemas.event_proposal import EventProposalCreate, EventProposalOut, CoordinatorReview, DeanReview
from app.auth.security import get_current_user, require_role
from app.services.booking_service import check_venue_availability, create_booking, release_booking
from app.services.notification_service import create_notification, notify_coordinators, notify_deans
from app.services import email_service
from app.models.notification import NotificationType

router = APIRouter(prefix="/events", tags=["Events"])


@router.post("/propose", response_model=EventProposalOut, status_code=201)
def propose_event(payload: EventProposalCreate, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if payload.start_datetime >= payload.end_datetime:
        raise HTTPException(status_code=400, detail="Start time must be before end time")
    if payload.start_datetime < datetime.utcnow():
        raise HTTPException(status_code=400, detail="Event cannot be scheduled in the past")

    conflicts = check_venue_availability(db, payload.venue_id, payload.start_datetime, payload.end_datetime)
    if conflicts:
        raise HTTPException(status_code=409, detail="Venue is already booked for the selected time slot. Please choose a different time or venue.")

    proposal = EventProposal(
        title=payload.title,
        description=payload.description,
        organizer_id=current_user.id,
        venue_id=payload.venue_id,
        faculty_incharge=payload.faculty_incharge,
        expected_participants=payload.expected_participants,
        start_datetime=payload.start_datetime,
        end_datetime=payload.end_datetime,
        event_type=payload.event_type,
        status=ProposalStatus.pending,
    )
    db.add(proposal)
    db.commit()
    db.refresh(proposal)

    create_notification(db, current_user.id, f"Your event proposal '{proposal.title}' has been submitted and is pending coordinator review.", NotificationType.proposal_submitted, proposal.id)
    coordinators = notify_coordinators(db, f"New event proposal '{proposal.title}' by {current_user.full_name} requires your review.", NotificationType.coordinator_review, proposal.id)

    email_service.send_proposal_submitted(current_user.email, current_user.full_name, proposal.title, proposal.id)
    for c in coordinators:
        email_service.send_coordinator_review_request(c.email, c.full_name, proposal.title, current_user.full_name, proposal.id)

    db.refresh(proposal)
    return proposal


@router.get("/my", response_model=list[EventProposalOut])
def my_proposals(db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    return db.query(EventProposal).filter(EventProposal.organizer_id == current_user.id).order_by(EventProposal.created_at.desc()).all()


@router.get("/pending", response_model=list[EventProposalOut])
def pending_proposals(db: Session = Depends(get_db), current_user: User = Depends(require_role(UserRole.coordinator, UserRole.dean, UserRole.admin))):
    if current_user.role == UserRole.coordinator:
        return db.query(EventProposal).filter(EventProposal.status == ProposalStatus.pending).order_by(EventProposal.created_at.asc()).all()
    elif current_user.role == UserRole.dean:
        return db.query(EventProposal).filter(EventProposal.status == ProposalStatus.coordinator_approved).order_by(EventProposal.created_at.asc()).all()
    return db.query(EventProposal).filter(EventProposal.status.in_([ProposalStatus.pending, ProposalStatus.coordinator_approved])).order_by(EventProposal.created_at.asc()).all()


@router.get("/all", response_model=list[EventProposalOut])
def all_proposals(
    status: str = Query(None),
    db: Session = Depends(get_db),
    current_user: User = Depends(require_role(UserRole.admin, UserRole.coordinator, UserRole.dean)),
):
    q = db.query(EventProposal)
    if status:
        q = q.filter(EventProposal.status == status)
    return q.order_by(EventProposal.created_at.desc()).all()


@router.get("/{proposal_id}", response_model=EventProposalOut)
def get_proposal(proposal_id: int, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    proposal = db.query(EventProposal).filter(EventProposal.id == proposal_id).first()
    if not proposal:
        raise HTTPException(status_code=404, detail="Proposal not found")
    if current_user.role == UserRole.student and proposal.organizer_id != current_user.id:
        raise HTTPException(status_code=403, detail="Access denied")
    return proposal


@router.patch("/{proposal_id}/coordinator-approve", response_model=EventProposalOut)
def coordinator_approve(proposal_id: int, review: CoordinatorReview, db: Session = Depends(get_db), current_user: User = Depends(require_role(UserRole.coordinator, UserRole.admin))):
    proposal = db.query(EventProposal).filter(EventProposal.id == proposal_id).first()
    if not proposal:
        raise HTTPException(status_code=404, detail="Proposal not found")
    if proposal.status != ProposalStatus.pending:
        raise HTTPException(status_code=400, detail=f"Proposal is not in pending state (current: {proposal.status})")

    conflicts = check_venue_availability(db, proposal.venue_id, proposal.start_datetime, proposal.end_datetime, proposal_id)
    if conflicts:
        raise HTTPException(status_code=409, detail="Venue conflict detected at approval time.")

    proposal.status = ProposalStatus.coordinator_approved
    proposal.coordinator_id = current_user.id
    proposal.coordinator_remarks = review.remarks
    proposal.coordinator_reviewed_at = datetime.utcnow()
    db.commit()
    db.refresh(proposal)

    create_notification(db, proposal.organizer_id, f"Your proposal '{proposal.title}' was approved by the coordinator and is now awaiting Dean review.", NotificationType.coordinator_review, proposal.id)
    notify_deans(db, f"Event proposal '{proposal.title}' by {proposal.organizer.full_name} requires Dean approval.", NotificationType.dean_review, proposal.id)

    return proposal


@router.patch("/{proposal_id}/coordinator-reject", response_model=EventProposalOut)
def coordinator_reject(proposal_id: int, review: CoordinatorReview, db: Session = Depends(get_db), current_user: User = Depends(require_role(UserRole.coordinator, UserRole.admin))):
    proposal = db.query(EventProposal).filter(EventProposal.id == proposal_id).first()
    if not proposal:
        raise HTTPException(status_code=404, detail="Proposal not found")
    if proposal.status != ProposalStatus.pending:
        raise HTTPException(status_code=400, detail="Proposal is not in pending state")

    proposal.status = ProposalStatus.rejected
    proposal.coordinator_id = current_user.id
    proposal.coordinator_remarks = review.remarks
    proposal.coordinator_reviewed_at = datetime.utcnow()
    db.commit()
    db.refresh(proposal)

    create_notification(db, proposal.organizer_id, f"Your proposal '{proposal.title}' was not approved by the coordinator. Remarks: {review.remarks or 'N/A'}", NotificationType.proposal_rejected, proposal.id)
    email_service.send_proposal_rejected(proposal.organizer.email, proposal.organizer.full_name, proposal.title, proposal.id, review.remarks or "")

    return proposal


@router.patch("/{proposal_id}/dean-approve", response_model=EventProposalOut)
def dean_approve(proposal_id: int, review: DeanReview, db: Session = Depends(get_db), current_user: User = Depends(require_role(UserRole.dean, UserRole.admin))):
    proposal = db.query(EventProposal).filter(EventProposal.id == proposal_id).first()
    if not proposal:
        raise HTTPException(status_code=404, detail="Proposal not found")
    if proposal.status != ProposalStatus.coordinator_approved:
        raise HTTPException(status_code=400, detail="Proposal must be coordinator-approved first")

    conflicts = check_venue_availability(db, proposal.venue_id, proposal.start_datetime, proposal.end_datetime, proposal_id)
    if conflicts:
        raise HTTPException(status_code=409, detail="Venue conflict detected at final approval time.")

    proposal.status = ProposalStatus.approved
    proposal.dean_id = current_user.id
    proposal.dean_remarks = review.remarks
    proposal.dean_reviewed_at = datetime.utcnow()
    db.commit()

    create_booking(db, proposal.venue_id, proposal.id, proposal.start_datetime, proposal.end_datetime)

    db.refresh(proposal)
    create_notification(db, proposal.organizer_id, f"🎉 Your event '{proposal.title}' has been fully approved! Venue is confirmed.", NotificationType.proposal_approved, proposal.id)
    email_service.send_proposal_approved(proposal.organizer.email, proposal.organizer.full_name, proposal.title, proposal.id, review.remarks or "")

    return proposal


@router.patch("/{proposal_id}/dean-reject", response_model=EventProposalOut)
def dean_reject(proposal_id: int, review: DeanReview, db: Session = Depends(get_db), current_user: User = Depends(require_role(UserRole.dean, UserRole.admin))):
    proposal = db.query(EventProposal).filter(EventProposal.id == proposal_id).first()
    if not proposal:
        raise HTTPException(status_code=404, detail="Proposal not found")
    if proposal.status != ProposalStatus.coordinator_approved:
        raise HTTPException(status_code=400, detail="Proposal must be coordinator-approved first")

    proposal.status = ProposalStatus.rejected
    proposal.dean_id = current_user.id
    proposal.dean_remarks = review.remarks
    proposal.dean_reviewed_at = datetime.utcnow()
    db.commit()
    db.refresh(proposal)

    create_notification(db, proposal.organizer_id, f"Your proposal '{proposal.title}' was declined by the Dean. Remarks: {review.remarks or 'N/A'}", NotificationType.proposal_rejected, proposal.id)
    email_service.send_proposal_rejected(proposal.organizer.email, proposal.organizer.full_name, proposal.title, proposal.id, review.remarks or "")

    return proposal
