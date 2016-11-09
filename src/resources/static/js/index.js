var ws = new WebSocket("ws://localhost:8080/chat");

var registeredUser;

ws.onopen = function() {
	console.log("WebSocket open");
}

ws.onmessage = function(message) {
	console.log(message.data);
	var json = JSON.parse(message.data);
	if (json.type == "message") {
		var cssClass = json.from == registeredUser ? "me" : "them";
		$("#messages").prepend(
				"<div class=\"message " + cssClass + "\"><p class=\"author\">"
						+ json.from + "</p><p class=\"message\">"
						+ json.message + "</p></div>");
	}
}

ws.onclose = function(event) {
	//unregister();
}

window.onunload = function() {
	//unregister();
}

$(function() {
	$("#name").focus();

	$('#name').keypress(function(e) {
		var key = e.which;
		if (key == 13) // the enter key code
		{
			register();
			return false;
		}
	});


	$('#message').keypress(function(e) {
		var key = e.which;
		if (key == 13) // the enter key code
		{
			send();
			return false;
		}
	});
});

function unregister() {
	if (registeredUser && registeredUser.length > 0) {
		var unregisterMessage = {
			"type" : "unregister",
			"name" : registeredUser
		}
		sendMessage(unregisterMessage);
	}
}

function send() {
	var message = $("#message").val();

	if (message.length > 0) {
		console.log("Message: " + message);
		var textMessage = {
			"type" : "message",
			"from" : registeredUser,
			"message" : message
		}
		sendMessage(textMessage);

		$("#message").val("").focus();
	}
}

function register() {
	var name = $("#name").val();
	registeredUser = name;

	if (name.length > 0) {
		console.log("Registering user [" + name + "]");
		var registerMessage = {
			"type" : "register",
			"name" : name
		};
		sendMessage(registerMessage);
		$("#message-input").toggleClass("hidden", false);
		$("#message").prop("disabled", false);
		$("#send").toggleClass("disabled", false);
		$("#send").prop("disabled", false);
		$("#message").focus();
		$("#register-input").toggleClass("hidden", true);
	}
}

function sendMessage(jsonMessage) {
	var messageString = JSON.stringify(jsonMessage);
	console.log("Send message [" + messageString + "]");
	ws.send(messageString);
}
