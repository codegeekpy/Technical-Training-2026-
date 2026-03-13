from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.venue import Venue
from app.models.event_proposal import EventProposal, ProposalStatus
from app.schemas.venue import VenueCreate, VenueUpdate, VenueOut, VenueAvailability
from app.auth.security import get_current_user, require_role
from app.models.user import UserRole
from app.services.booking_service import check_venue_availability

router = APIRouter(prefix="/venues", tags=["Venues"])


@router.get("/", response_model=list[VenueOut])
def list_venues(active_only: bool = True, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    q = db.query(Venue)
    if active_only:
        q = q.filter(Venue.is_active == True)
    return q.all()


@router.get("/{venue_id}", response_model=VenueOut)
def get_venue(venue_id: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    venue = db.query(Venue).filter(Venue.id == venue_id).first()
    if not venue:
        raise HTTPException(status_code=404, detail="Venue not found")
    return venue


@router.get("/{venue_id}/availability", response_model=VenueAvailability)
def check_availability(
    venue_id: int,
    start_datetime: datetime = Query(...),
    end_datetime: datetime = Query(...),
    exclude_proposal_id: int = Query(None),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    venue = db.query(Venue).filter(Venue.id == venue_id).first()
    if not venue:
        raise HTTPException(status_code=404, detail="Venue not found")
    if start_datetime >= end_datetime:
        raise HTTPException(status_code=400, detail="start_datetime must be before end_datetime")

    conflicts = check_venue_availability(db, venue_id, start_datetime, end_datetime, exclude_proposal_id)
    conflicting_events = []
    for booking in conflicts:
        prop = booking.event_proposal
        conflicting_events.append({
            "proposal_id": prop.id,
            "event_title": prop.title,
            "organizer": prop.organizer.full_name,
            "start": booking.start_datetime.isoformat(),
            "end": booking.end_datetime.isoformat(),
        })
    return VenueAvailability(
        venue_id=venue_id,
        venue_name=venue.name,
        is_available=len(conflicts) == 0,
        conflicting_events=conflicting_events,
    )


@router.post("/", response_model=VenueOut, status_code=201)
def create_venue(payload: VenueCreate, db: Session = Depends(get_db), current_user=Depends(require_role(UserRole.admin))):
    venue = Venue(**payload.model_dump())
    db.add(venue)
    db.commit()
    db.refresh(venue)
    return venue


@router.patch("/{venue_id}", response_model=VenueOut)
def update_venue(venue_id: int, payload: VenueUpdate, db: Session = Depends(get_db), current_user=Depends(require_role(UserRole.admin))):
    venue = db.query(Venue).filter(Venue.id == venue_id).first()
    if not venue:
        raise HTTPException(status_code=404, detail="Venue not found")
    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(venue, field, value)
    db.commit()
    db.refresh(venue)
    return venue
