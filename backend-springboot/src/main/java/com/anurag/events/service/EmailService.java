package com.anurag.events.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            emailSender.send(message);
            log.info("[EMAIL SENT] To: {} | Subject: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("[EMAIL FAILED] To: {} | Error: {}", toEmail, e.getMessage());
        }
    }

    public void sendProposalSubmitted(String organizerEmail, String organizerName, String eventTitle, Long proposalId) {
        String subject = "Event Proposal Submitted - " + eventTitle;
        String html = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;">
              <div style="background: #003366; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
                <h2 style="margin:0;">Anurag University Event Management</h2>
              </div>
              <div style="padding: 24px;">
                <p>Dear <strong>%s</strong>,</p>
                <p>Your event proposal <strong>"%s"</strong> (ID: #%d) has been submitted successfully.</p>
                <p>It is now <strong>pending coordinator review</strong>. You will receive a notification once the coordinator acts on it.</p>
                <div style="background: #FFF8F0; border-left: 4px solid #FF6B2B; padding: 12px; margin: 16px 0;">
                  <p style="margin:0;">Track your proposal status in the <a href="%s/my-proposals" style="color:#003366;">Event Management Portal</a></p>
                </div>
                <p>Regards,<br>Anurag University Events Team</p>
              </div>
            </div>
            """, organizerName, eventTitle, proposalId, frontendUrl);
        sendEmail(organizerEmail, subject, html);
    }

    public void sendCoordinatorReviewRequest(String coordinatorEmail, String coordinatorName, String eventTitle, String organizerName, Long proposalId) {
        String subject = "Action Required: Review Event Proposal - " + eventTitle;
        String html = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;">
              <div style="background: #003366; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
                <h2 style="margin:0;">Anurag University Event Management</h2>
              </div>
              <div style="padding: 24px;">
                <p>Dear <strong>%s</strong>,</p>
                <p>A new event proposal requires your review:</p>
                <ul>
                  <li><strong>Event:</strong> %s</li>
                  <li><strong>Organizer:</strong> %s</li>
                  <li><strong>Proposal ID:</strong> #%d</li>
                </ul>
                <div style="background: #FFF8F0; border-left: 4px solid #FF6B2B; padding: 12px; margin: 16px 0;">
                  <a href="%s/coordinator/proposals/%d" style="background:#003366; color:white; padding: 10px 20px; text-decoration:none; border-radius:4px; display:inline-block;">Review Proposal</a>
                </div>
                <p>Regards,<br>Anurag University Events Team</p>
              </div>
            </div>
            """, coordinatorName, eventTitle, organizerName, proposalId, frontendUrl, proposalId);
        sendEmail(coordinatorEmail, subject, html);
    }

    public void sendProposalApproved(String organizerEmail, String organizerName, String eventTitle, Long proposalId, String remarks) {
        String subject = "Event Proposal Approved ✓ - " + eventTitle;
        String remarkHtml = (remarks != null && !remarks.isEmpty())
                ? String.format("<div style=\"background:#f0fff0; border-left: 4px solid #1a7a3a; padding: 12px;\"><strong>Remarks:</strong> %s</div>", remarks)
                : "";

        String html = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;">
              <div style="background: #1a7a3a; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
                <h2 style="margin:0;">✓ Event Approved</h2>
              </div>
              <div style="padding: 24px;">
                <p>Dear <strong>%s</strong>,</p>
                <p>Great news! Your event proposal <strong>"%s"</strong> (ID: #%d) has been <strong>approved</strong>.</p>
                %s
                <p>The venue has been booked and confirmed for your event. Please ensure all preparations are in order.</p>
                <p>Regards,<br>Anurag University Events Team</p>
              </div>
            </div>
            """, organizerName, eventTitle, proposalId, remarkHtml);
        sendEmail(organizerEmail, subject, html);
    }

    public void sendProposalRejected(String organizerEmail, String organizerName, String eventTitle, Long proposalId, String remarks) {
        String subject = "Event Proposal Update - " + eventTitle;
        String remarkHtml = (remarks != null && !remarks.isEmpty())
                ? String.format("<div style=\"background:#fff0f0; border-left: 4px solid #8b1a1a; padding: 12px;\"><strong>Reason:</strong> %s</div>", remarks)
                : "";

        String html = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;">
              <div style="background: #8b1a1a; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
                <h2 style="margin:0;">Event Proposal Not Approved</h2>
              </div>
              <div style="padding: 24px;">
                <p>Dear <strong>%s</strong>,</p>
                <p>Your event proposal <strong>"%s"</strong> (ID: #%d) could not be approved at this time.</p>
                %s
                <p>You may submit a revised proposal addressing the feedback.</p>
                <p>Regards,<br>Anurag University Events Team</p>
              </div>
            </div>
            """, organizerName, eventTitle, proposalId, remarkHtml);
        sendEmail(organizerEmail, subject, html);
    }
}
