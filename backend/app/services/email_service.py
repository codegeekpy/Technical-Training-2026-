import smtplib
import logging
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from app.config import get_settings

settings = get_settings()
logger = logging.getLogger(__name__)


def send_email(to_email: str, subject: str, html_body: str):
    if not settings.mail_username or not settings.mail_password:
        logger.warning(f"[EMAIL SKIPPED - No SMTP config] To: {to_email} | Subject: {subject}")
        return

    try:
        msg = MIMEMultipart("alternative")
        msg["Subject"] = subject
        msg["From"] = f"{settings.mail_from_name} <{settings.mail_from}>"
        msg["To"] = to_email
        msg.attach(MIMEText(html_body, "html"))

        with smtplib.SMTP(settings.mail_server, settings.mail_port) as server:
            server.ehlo()
            if settings.mail_starttls:
                server.starttls()
            server.login(settings.mail_username, settings.mail_password)
            server.sendmail(settings.mail_from, to_email, msg.as_string())
        logger.info(f"[EMAIL SENT] To: {to_email} | Subject: {subject}")
    except Exception as e:
        logger.error(f"[EMAIL FAILED] To: {to_email} | Error: {e}")


def send_proposal_submitted(organizer_email: str, organizer_name: str, event_title: str, proposal_id: int):
    subject = f"Event Proposal Submitted - {event_title}"
    html = f"""
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;">
      <div style="background: #003366; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
        <h2 style="margin:0;">Anurag University Event Management</h2>
      </div>
      <div style="padding: 24px;">
        <p>Dear <strong>{organizer_name}</strong>,</p>
        <p>Your event proposal <strong>"{event_title}"</strong> (ID: #{proposal_id}) has been submitted successfully.</p>
        <p>It is now <strong>pending coordinator review</strong>. You will receive a notification once the coordinator acts on it.</p>
        <div style="background: #FFF8F0; border-left: 4px solid #FF6B2B; padding: 12px; margin: 16px 0;">
          <p style="margin:0;">Track your proposal status in the <a href="{settings.frontend_url}/my-proposals" style="color:#003366;">Event Management Portal</a></p>
        </div>
        <p>Regards,<br>Anurag University Events Team</p>
      </div>
    </div>
    """
    send_email(organizer_email, subject, html)


def send_coordinator_review_request(coordinator_email: str, coordinator_name: str, event_title: str, organizer_name: str, proposal_id: int):
    subject = f"Action Required: Review Event Proposal - {event_title}"
    html = f"""
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;">
      <div style="background: #003366; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
        <h2 style="margin:0;">Anurag University Event Management</h2>
      </div>
      <div style="padding: 24px;">
        <p>Dear <strong>{coordinator_name}</strong>,</p>
        <p>A new event proposal requires your review:</p>
        <ul>
          <li><strong>Event:</strong> {event_title}</li>
          <li><strong>Organizer:</strong> {organizer_name}</li>
          <li><strong>Proposal ID:</strong> #{proposal_id}</li>
        </ul>
        <div style="background: #FFF8F0; border-left: 4px solid #FF6B2B; padding: 12px; margin: 16px 0;">
          <a href="{settings.frontend_url}/coordinator/proposals/{proposal_id}" style="background:#003366; color:white; padding: 10px 20px; text-decoration:none; border-radius:4px; display:inline-block;">Review Proposal</a>
        </div>
        <p>Regards,<br>Anurag University Events Team</p>
      </div>
    </div>
    """
    send_email(coordinator_email, subject, html)


def send_proposal_approved(organizer_email: str, organizer_name: str, event_title: str, proposal_id: int, remarks: str = ""):
    subject = f"Event Proposal Approved ✓ - {event_title}"
    html = f"""
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;">
      <div style="background: #1a7a3a; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
        <h2 style="margin:0;">✓ Event Approved</h2>
      </div>
      <div style="padding: 24px;">
        <p>Dear <strong>{organizer_name}</strong>,</p>
        <p>Great news! Your event proposal <strong>"{event_title}"</strong> (ID: #{proposal_id}) has been <strong>approved</strong>.</p>
        {f'<div style="background:#f0fff0; border-left: 4px solid #1a7a3a; padding: 12px;"><strong>Remarks:</strong> {remarks}</div>' if remarks else ""}
        <p>The venue has been booked and confirmed for your event. Please ensure all preparations are in order.</p>
        <p>Regards,<br>Anurag University Events Team</p>
      </div>
    </div>
    """
    send_email(organizer_email, subject, html)


def send_proposal_rejected(organizer_email: str, organizer_name: str, event_title: str, proposal_id: int, remarks: str = ""):
    subject = f"Event Proposal Update - {event_title}"
    html = f"""
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;">
      <div style="background: #8b1a1a; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
        <h2 style="margin:0;">Event Proposal Not Approved</h2>
      </div>
      <div style="padding: 24px;">
        <p>Dear <strong>{organizer_name}</strong>,</p>
        <p>Your event proposal <strong>"{event_title}"</strong> (ID: #{proposal_id}) could not be approved at this time.</p>
        {f'<div style="background:#fff0f0; border-left: 4px solid #8b1a1a; padding: 12px;"><strong>Reason:</strong> {remarks}</div>' if remarks else ""}
        <p>You may submit a revised proposal addressing the coordinator's feedback.</p>
        <p>Regards,<br>Anurag University Events Team</p>
      </div>
    </div>
    """
    send_email(organizer_email, subject, html)
