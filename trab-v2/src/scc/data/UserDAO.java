package scc.data;

import java.util.List;

/**
 * Represents a User, as stored in the database
 */
public class UserDAO {
	private String id;
	private String name;
	private String pwd;
	private String photoId;
	private List<String> channelIds;

	public UserDAO() {
	}
	
	public UserDAO( User u) {
		this(u.getId(), u.getName(), u.getPwd(), u.getPhotoId(), u.getChannelIds());
	}

	public UserDAO(String id, String name, String pwd, String photoId, List<String> channelIds) {
		super();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.channelIds = channelIds;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
	public List<String> getChannelIds() {
		return channelIds;
	}
	public void setChannelIds(List<String> channelIds) {
		this.channelIds = channelIds;
	}
	public User toUser() {
		return new User( id, name, pwd, photoId, channelIds);
	}
	@Override
	public String toString() {
		return "UserDAO [ id=" + id + ", name=" + name + ", pwd=" + pwd
				+ ", photoId=" + photoId + ", channelIds=" + channelIds.toString() + "]";
	}

}
