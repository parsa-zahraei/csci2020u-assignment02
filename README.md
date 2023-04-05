# Assignment 02 - Web Chat Server (Instructions)
> Course: CSCI 2020U: Software Systems Development and Integration

This is the template for your Assignment 02.

## Overview
You want to create a web chat server with multiple rooms. This will allow you and your friends to chat privately.

- Check the `Canvas/Assingments/Assignment 02` for more the detailed instructions.

### WebChatServer - Endpoints

**Connect to the websocket**

From the `ChatServer` class. This will create a new client connect to the web server. The server and client communicate using `json` messages.
- `ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/{roomID}`


**GET a new (unique) room code**

From the `ChatServlet` class. This will return a `text/plain` content type.
- `http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet`
See a sample of the response data:
```
1B9FN
```

### WebChatServer - client

Your client is in the `webapp` folder, when started the application will run at `http://localhost:8080/WSChatServer-1.0-SNAPSHOT/`; which will load the `index.html` file.

Your client-side code will be in the `js/main.js` javascript file.

> Obs. Feel free to create other helper classes as you see fit.
> 



