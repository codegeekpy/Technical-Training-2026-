from pydantic import BaseModel
from typing import Optional
from app.models.venue import VenueType


class VenueCreate(BaseModel):
    name: str
    venue_type: VenueType
    capacity: int
    location: str
    description: Optional[str] = None


class VenueUpdate(BaseModel):
    name: Optional[str] = None
    venue_type: Optional[VenueType] = None
    capacity: Optional[int] = None
    location: Optional[str] = None
    description: Optional[str] = None
    is_active: Optional[bool] = None


class VenueOut(BaseModel):
    id: int
    name: str
    venue_type: VenueType
    capacity: int
    location: str
    description: Optional[str]
    is_active: bool

    class Config:
        from_attributes = True


class VenueAvailability(BaseModel):
    venue_id: int
    venue_name: str
    is_available: bool
    conflicting_events: list = []
