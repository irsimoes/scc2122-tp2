package scc.data;

import org.bson.types.ObjectId;

public class MessageDAO {
    private ObjectId _id; //record id
	private long _ts; //timestamp
    private String id;
	private String replyTo;
    private String channel;
    private String user;
    private String text;
    private String imageId;

    public MessageDAO() {
	}

	public MessageDAO(String id, String replyTo, String channel, String user, String text, String imageId) {
		super();
		this.id = id;
		this.replyTo = replyTo;
		this.channel = channel;
		this.user = user;
		this.text = text;
		this.imageId = imageId;
	}

    public MessageDAO(Message m) {
		this(m.getId(), m.getReplyTo(), m.getChannel(), m.getUser(), m.getText(), m.getImageId());
	}

    public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public long get_ts() {
		return _ts;
	}

	public void set_ts(long _ts) {
		this._ts = _ts;
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
		return "MessageDAO [_id=" + _id + ", _ts=" + _ts + ",id=" + id + ", replyTo=" + replyTo + ", channel=" + channel + ", user=" + user + ", text="
				+ text +  ", imageId=" + imageId + "]";
	}
	
}
