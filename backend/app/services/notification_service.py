from sqlalchemy.orm import Session
from app.models.notification import Notification, NotificationType


def create_notification(db: Session, user_id: int, message: str, notification_type: NotificationType, event_proposal_id: int = None):
    notif = Notification(
        user_id=user_id,
        message=message,
        notification_type=notification_type,
        event_proposal_id=event_proposal_id,
    )
    db.add(notif)
    db.commit()
    db.refresh(notif)
    return notif


def notify_coordinators(db: Session, message: str, notification_type: NotificationType, event_proposal_id: int = None):
    from app.models.user import User, UserRole
    coordinators = db.query(User).filter(User.role == UserRole.coordinator, User.is_active == True).all()
    for coordinator in coordinators:
        create_notification(db, coordinator.id, message, notification_type, event_proposal_id)
    return coordinators


def notify_deans(db: Session, message: str, notification_type: NotificationType, event_proposal_id: int = None):
    from app.models.user import User, UserRole
    deans = db.query(User).filter(User.role == UserRole.dean, User.is_active == True).all()
    for dean in deans:
        create_notification(db, dean.id, message, notification_type, event_proposal_id)
    return deans
