from datetime import datetime
from sqlalchemy.orm import Session
from app.models.booking import Booking
from app.models.event_proposal import EventProposal


def check_venue_availability(db: Session, venue_id: int, start_dt: datetime, end_dt: datetime, exclude_proposal_id: int = None) -> list:
    query = db.query(Booking).filter(
        Booking.venue_id == venue_id,
        Booking.start_datetime < end_dt,
        Booking.end_datetime > start_dt,
    )
    if exclude_proposal_id:
        query = query.filter(Booking.event_proposal_id != exclude_proposal_id)
    conflicts = query.all()
    return conflicts


def create_booking(db: Session, venue_id: int, event_proposal_id: int, start_dt: datetime, end_dt: datetime) -> Booking:
    booking = Booking(
        venue_id=venue_id,
        event_proposal_id=event_proposal_id,
        start_datetime=start_dt,
        end_datetime=end_dt,
    )
    db.add(booking)
    db.commit()
    db.refresh(booking)
    return booking


def release_booking(db: Session, event_proposal_id: int):
    booking = db.query(Booking).filter(Booking.event_proposal_id == event_proposal_id).first()
    if booking:
        db.delete(booking)
        db.commit()
