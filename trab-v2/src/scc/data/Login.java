package scc.data;

public class Login {
    private String id;
	private String pwd;

    public Login() {
	}

	public Login(String id, String pwd) {
		super();
		this.id = id;
		this.pwd = pwd;
	}

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

}
