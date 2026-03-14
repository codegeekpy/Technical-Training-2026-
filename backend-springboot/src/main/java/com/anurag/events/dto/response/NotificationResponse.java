package com.anurag.events.dto.response;

import com.anurag.events.entity.Notification.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long user_id;
    private Long event_proposal_id;
    private NotificationType notification_type;
    private String message;
    private boolean is_read;
    private OffsetDateTime created_at;
}
