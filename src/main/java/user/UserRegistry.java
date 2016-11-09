package user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.socket.WebSocketSession;

public class UserRegistry {
	private Map<String, User> usersByName;
	private Map<WebSocketSession, User> usersBySession;

	private static UserRegistry INSTANCE = new UserRegistry();

	public UserRegistry() {
		usersByName = new HashMap<String, User>();
		usersBySession = new HashMap<WebSocketSession, User>();
	}

	public static UserRegistry getInstance() {
		return INSTANCE;
	}

	public boolean addUser(User user) {
		try {
			usersByName.put(user.getName(), user);
			usersBySession.put(user.getSession(), user);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean removeUser(User user) {
		usersByName.remove(user.getName());
		usersBySession.remove(user.getSession());
		
		return true;
	}

	public User getUserByName(String name) {
		if (name == null) {
			return null;
		}
		return usersByName.get(name);
	}

	public User getUserBySession(WebSocketSession session) {
		if (session == null) {
			return null;
		}
		return usersBySession.get(session);
	}

	public Collection<User> getUsers() {
		return usersByName.values();
	}
}