package scc.data;

public class Message {
    private String id;
	private String replyTo;
    private String channel;
    private String user;
    private String text;
    private String imageId;

	public Message() {
	}

	public Message(String id, String replyTo, String channel, String user, String text, String imageId) {
		super();
		this.id = id;
		this.replyTo = replyTo;
		this.channel = channel;
		this.user = user;
		this.text = text;
		this.imageId = imageId;
	}

	public Message(MessageDAO m) {
		super();
		this.id = m.getId();
		this.replyTo = m.getReplyTo();
		this.channel = m.getChannel();
		this.user = m.getUser();
		this.text = m.getText();
		this.imageId = m.getImageId();
    }


	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReplyTo() {
		return this.replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getChannel() {
		return this.channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getImageId() {
		return this.imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", replyTo=" + replyTo + ", channel=" + channel + ", user=" + user + ", text="
				+ text +  ", imageId=" + imageId + "]";
	}
	
}
