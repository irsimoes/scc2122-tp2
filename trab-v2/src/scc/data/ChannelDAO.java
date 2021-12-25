package scc.data;

import java.util.List;

public class ChannelDAO {
    private String id;
    private String name;
    private String owner;
    private boolean publicChannel;
    private List<String> members;

    public ChannelDAO() {
    }

    public ChannelDAO(Channel c) {
        this(c.getId(), c.getName(), c.getOwner(), c.isPublicChannel(), c.getMembers());
    }

    public ChannelDAO(String id, String name, String owner, boolean publicChannel, List<String> members) {
        super();
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.publicChannel = publicChannel;
        this.members = members;
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

    public List<String> getMembers() {
        return this.members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    @Override
	public String toString() {
		return "ChannelDAO [id=" + id + ", name=" + name + ", owner=" 
            + owner + ", publicChannel=" + publicChannel + ", members=" + members + "]";
	}
}