package com.civictrace.realtime;

import com.civictrace.issue.IssueStatus;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class IssueEventPublisherTest {

    @Test
    void publishesOnlyThePublicIssueEventPayloadToThePublicTopic() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        IssueEventPublisher publisher = new IssueEventPublisher(messagingTemplate);
        IssueChangeEvent event = new IssueChangeEvent("STATUS_CHANGED", UUID.randomUUID(), "Streetlight fixed", IssueStatus.RESOLVED, Instant.now());

        publisher.publishAfterCommit(event);

        verify(messagingTemplate).convertAndSend("/topic/issues", event);
    }
}
