import enum
from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, Enum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class ProposalStatus(str, enum.Enum):
    draft = "draft"
    pending = "pending"
    coordinator_approved = "coordinator_approved"
    approved = "approved"
    rejected = "rejected"


class EventProposal(Base):
    __tablename__ = "event_proposals"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    organizer_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    venue_id = Column(Integer, ForeignKey("venues.id"), nullable=False)
    faculty_incharge = Column(String(255), nullable=False)
    expected_participants = Column(Integer, nullable=False)
    start_datetime = Column(DateTime(timezone=True), nullable=False)
    end_datetime = Column(DateTime(timezone=True), nullable=False)
    event_type = Column(String(100), nullable=True)
    status = Column(Enum(ProposalStatus), default=ProposalStatus.pending, nullable=False)
    coordinator_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    coordinator_remarks = Column(Text, nullable=True)
    coordinator_reviewed_at = Column(DateTime(timezone=True), nullable=True)
    dean_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    dean_remarks = Column(Text, nullable=True)
    dean_reviewed_at = Column(DateTime(timezone=True), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    organizer = relationship("User", back_populates="proposals", foreign_keys=[organizer_id])
    venue = relationship("Venue", back_populates="proposals")
    coordinator = relationship("User", foreign_keys=[coordinator_id])
    dean = relationship("User", foreign_keys=[dean_id])
    booking = relationship("Booking", back_populates="event_proposal", uselist=False)
