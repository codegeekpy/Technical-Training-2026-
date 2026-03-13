import enum
from sqlalchemy import Column, Integer, String, Boolean, Enum, Text
from sqlalchemy.orm import relationship
from app.database import Base


class VenueType(str, enum.Enum):
    auditorium = "auditorium"
    seminar_hall = "seminar_hall"
    lab = "lab"
    conference_room = "conference_room"
    open_ground = "open_ground"


class Venue(Base):
    __tablename__ = "venues"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    venue_type = Column(Enum(VenueType), nullable=False)
    capacity = Column(Integer, nullable=False)
    location = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    is_active = Column(Boolean, default=True)

    proposals = relationship("EventProposal", back_populates="venue")
    bookings = relationship("Booking", back_populates="venue")
