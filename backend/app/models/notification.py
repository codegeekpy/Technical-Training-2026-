import enum
from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, Enum, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class NotificationType(str, enum.Enum):
    proposal_submitted = "proposal_submitted"
    proposal_approved = "proposal_approved"
    proposal_rejected = "proposal_rejected"
    coordinator_review = "coordinator_review"
    dean_review = "dean_review"
    general = "general"


class Notification(Base):
    __tablename__ = "notifications"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    event_proposal_id = Column(Integer, ForeignKey("event_proposals.id"), nullable=True)
    notification_type = Column(Enum(NotificationType), default=NotificationType.general)
    message = Column(String(500), nullable=False)
    is_read = Column(Boolean, default=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="notifications")
