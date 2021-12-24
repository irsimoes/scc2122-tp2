package scc.data;

import org.bson.types.ObjectId;
import java.util.Arrays;

/**
 * Represents a User, as stored in the database
 */
public class UserDAO {
	private ObjectId _id; //record id
	private String id;
	private String name;
	private String pwd;
	private String photoId;
	private String[] channelIds;

	public UserDAO() {
	}
	
	public UserDAO( User u) {
		this(u.getId(), u.getName(), u.getPwd(), u.getPhotoId(), u.getChannelIds());
	}

	public UserDAO(String id, String name, String pwd, String photoId, String[] channelIds) {
		super();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.channelIds = channelIds;
	}
	public ObjectId get_id() {
		return _id;
	}
	public void set_id(ObjectId _id) {
		this._id = _id;
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
	public String[] getChannelIds() {
		return channelIds == null ? new String[0] : channelIds ;
	}
	public void setChannelIds(String[] channelIds) {
		this.channelIds = channelIds;
	}
	public User toUser() {
		return new User( id, name, pwd, photoId, channelIds == null ? null : Arrays.copyOf(channelIds,channelIds.length));
	}
	@Override
	public String toString() {
		return "UserDAO [_id=" + _id + ", id=" + id + ", name=" + name + ", pwd=" + pwd
				+ ", photoId=" + photoId + ", channelIds=" + Arrays.toString(channelIds) + "]";
	}

}
