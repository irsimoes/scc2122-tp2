package scc.data;

import org.bson.types.ObjectId;

public class ChannelDAO {
    private ObjectId _id; // record id
    private String id;
    private String name;
    private String owner;
    private boolean publicChannel;
    private String[] members;

    public ChannelDAO() {
    }

    public ChannelDAO(Channel c) {
        this(c.getId(), c.getName(), c.getOwner(), c.isPublicChannel(), c.getMembers());
    }

    public ChannelDAO(String id, String name, String owner, boolean publicChannel, String[] members) {
        super();
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.publicChannel = publicChannel;
        this.members = members;
    }

    public ObjectId get_id() {
        return this._id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isPublicChannel() {
        return this.publicChannel;
    }

    public void setPublicChannel(boolean publicChannel) {
        this.publicChannel = publicChannel;
    }

    public String[] getMembers() {
        return this.members;
    }

    public void setMembers(String[] members) {
        this.members = members;
    }

    @Override
	public String toString() {
		return "ChannelDAO [_id=" + _id + "id=" + id + ", name=" + name + ", owner=" 
            + owner + ", publicChannel=" + publicChannel + ", members=" + members + "]";
	}
}