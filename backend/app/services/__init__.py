from app.services.email_service import (
    send_proposal_submitted,
    send_coordinator_review_request,
    send_proposal_approved,
    send_proposal_rejected,
)
from app.services.booking_service import check_venue_availability, create_booking, release_booking
from app.services.notification_service import create_notification, notify_coordinators, notify_deans
