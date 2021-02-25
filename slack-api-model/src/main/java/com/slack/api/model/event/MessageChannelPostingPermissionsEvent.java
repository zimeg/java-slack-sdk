package com.slack.api.model.event;

import lombok.Data;

/**
 * https://api.slack.com/events/message/channel_posting_permissions
 */
@Data
public class MessageChannelPostingPermissionsEvent implements Event {

	public static final String TYPE_NAME = "message";
	public static final String SUBTYPE_NAME = "channel_posting_permissions";

	private final String type = TYPE_NAME;
	private final String subtype = SUBTYPE_NAME;

	private String user;
	private String channel;
	private String channelType; // "channel"

	private String text;

	private String ts;
	private String eventTs;
}
