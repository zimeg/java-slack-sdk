package com.slack.api.model.event;

import com.slack.api.model.assistant.AssistantThread;
import lombok.Data;

@Data
public class AssistantThreadContextChangedEvent implements Event {

    public static final String TYPE_NAME = "assistant_thread_context_changed";

    private final String type = TYPE_NAME;
    private AssistantThread assistantThread;
    private String eventTs;
}