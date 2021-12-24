package scc.data;

/**
 * Represents a User, as returned to the clients
 */
public class UserProfile {
	private String id;
	private String name;
	private String photoId;

	public UserProfile() {
	}

	public UserProfile(String id, String name, String photoId) {
		super();
		this.id = id;
		this.name = name;
		this.photoId = photoId;
	}
	
	public UserProfile(UserDAO u) {
		super();
		this.id = u.getId();
		this.name = u.getName();
		this.photoId = u.getPhotoId();
	}

    public UserProfile(User u) {
		super();
		this.id = u.getId();
		this.name = u.getName();
		this.photoId = u.getPhotoId();
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
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

}
