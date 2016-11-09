package user;

import org.springframework.web.socket.WebSocketSession;

public class User {
	private String name;
	private WebSocketSession session;
	
	public User(String name, WebSocketSession session) {
		super();
		this.name = name;
		this.session = session;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", session=" + session + "]";
	}

	public String getName() {
		return name;
	}

	public WebSocketSession getSession() {
		return session;
	}
	
}