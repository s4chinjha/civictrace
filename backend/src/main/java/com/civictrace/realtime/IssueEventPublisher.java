package com.civictrace.realtime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class IssueEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public IssueEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(IssueChangeEvent event) {
        messagingTemplate.convertAndSend("/topic/issues", event);
    }
}
