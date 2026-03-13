from datetime import datetime
from typing import Optional
from pydantic import BaseModel
from app.models.event_proposal import ProposalStatus
from app.schemas.user import UserOut
from app.schemas.venue import VenueOut


class EventProposalCreate(BaseModel):
    title: str
    description: Optional[str] = None
    venue_id: int
    faculty_incharge: str
    expected_participants: int
    start_datetime: datetime
    end_datetime: datetime
    event_type: Optional[str] = None


class EventProposalOut(BaseModel):
    id: int
    title: str
    description: Optional[str]
    organizer: UserOut
    venue: VenueOut
    faculty_incharge: str
    expected_participants: int
    start_datetime: datetime
    end_datetime: datetime
    event_type: Optional[str]
    status: ProposalStatus
    coordinator_remarks: Optional[str]
    coordinator_reviewed_at: Optional[datetime]
    dean_remarks: Optional[str]
    dean_reviewed_at: Optional[datetime]
    created_at: datetime

    class Config:
        from_attributes = True


class CoordinatorReview(BaseModel):
    remarks: Optional[str] = None


class DeanReview(BaseModel):
    remarks: Optional[str] = None
