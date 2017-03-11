package de.neiky.chatserver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import user.User;
import user.UserRegistry;

public class ChatMessageHandler extends TextWebSocketHandler {
	private final Logger logger = Logger.getLogger(getClass());

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		String msg = message.getPayload();
		System.out.println(session.getId() + ": " + message.getPayload());
//		logger.debug(session.getId() + ": " + message.getPayload());

		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(msg);
			String type = (String) json.get("type");
			String payload = (String) json.get("payload");

			processMessage(session, type, json);

		} catch (ParseException e) {
			System.out.println("Bad message! " + session.getId() + ": " + message.getPayload());
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		unregisterUser(session, null);
	}

	private void processMessage(WebSocketSession session, String type, JSONObject jsonMessage) {
		switch (type) {
		case "register":
			registerUser(session, (String) jsonMessage.get("name"));
			break;

		case "unregister":
			unregisterUser(session, (String) jsonMessage.get("name"));
			break;

		case "message":
			forwardMessage(null, jsonMessage.toJSONString());
			break;

		default:
			break;
		}
	}

	private void registerUser(WebSocketSession session, String name) {
		if (UserRegistry.getInstance().getUserByName(name) == null) {
			User user = new User(name, session);
			if (UserRegistry.getInstance().addUser(user)) {
				sendRegisterSuccess(session, name);
				updateUserList();
			} else {
				System.err.println("User cannot be added!");
				sendRegisterError(session, name);
			}
		} else {
			System.err.println("User already exists!");
			sendRegisterError(session, name);
		}
	}

	/**
	 * Send an updated list of registered users to all other users
	 */
	private void updateUserList() {
		Collection<User> users = UserRegistry.getInstance().getUsers();
		List<String> userlist = users.stream().map(u -> u.getName()).collect(Collectors.toList());
		
		JSONObject obj = new JSONObject();
		obj.put("type", "userlist");
		obj.put("userlist", userlist);
		
		UserRegistry.getInstance().getUsers().forEach(user -> {
			sendMessage(user.getSession(), obj.toJSONString());
		});
	}

	private void unregisterUser(WebSocketSession session, String name) {
		User user = UserRegistry.getInstance().getUserBySession(session);
		if (user != null) {
			UserRegistry.getInstance().removeUser(user);
		}
		updateUserList();
	}

	private void forwardMessage(String to, String message) {
		UserRegistry.getInstance().getUsers().forEach(user -> {
			sendMessage(user.getSession(), message);
		});
	}

	private void sendRegisterSuccess(WebSocketSession session, String name) {
		String success = "User [" + name + "] successfully registered.";

		JSONObject obj = new JSONObject();
		obj.put("type", "success");
		obj.put("success", "registerSuccess");

		sendMessage(session, obj.toJSONString());
	}

	private void sendMessage(WebSocketSession session, String message) {
		try {
			session.sendMessage(new TextMessage(message));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendRegisterError(WebSocketSession session, String name) {
		String error = "Unable to register user with name " + name;

		JSONObject obj = new JSONObject();
		obj.put("type", "error");
		obj.put("error", "registerError");

		sendMessage(session, obj.toJSONString());
	}
}