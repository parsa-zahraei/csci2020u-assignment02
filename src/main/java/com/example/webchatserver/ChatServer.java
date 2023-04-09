package com.example.webchatserver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;


/**
 * This class represents a web socket server, a new connection is created and it receives a roomID as a parameter
 * **/
@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {

    // contains a static List of ChatRoom used to control the existing rooms and their users
    static List<ChatRoom> rooms = new ArrayList<>();


    // Instance of chatRoom that represents the current room that the user is in
    static ChatRoom currentRoom;


    // you may add other attributes as you see fit


    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {

        String userID = session.getId();

        boolean newRoom = true;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.now();

        //check if room exists
        for (ChatRoom room: rooms)
        {
            if (roomID.equals(room.getCode()))
            {
                newRoom = false;

                currentRoom = room;

                currentRoom.setUserName(userID, "");

            }
        }

        if (newRoom)
        {
            currentRoom = new ChatRoom(roomID, userID);

            rooms.add(currentRoom);

            //currentRoom.getChatHistory().add(String.format("{\"type\": \"chat\", \"message\":\"[%s] Room %s created:",dtf.format(localTime), roomID));
        }


        String initMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s](Server %s): Please state your username to begin.\"}",dtf.format(localTime),roomID);

        session.getBasicRemote().sendText(initMessage);

//        accessing the roomID parameter
        System.out.println(roomID);

    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId();

        String userName = currentRoom.getUsers().get(userId);
        // do things for when the connection closes

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.now();

        String broadMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (Server %s): %s left the room\"}",dtf.format(localTime), currentRoom.getCode(), userName);

        currentRoom.getChatHistory().add(broadMessage);

        for (Session peer : session.getOpenSessions())
        {
            if ((peer.getId() != userId ) && (currentRoom.getUsers().get(peer.getId()) != null))
            {
                currentRoom.removeUser(peer.getId());
                peer.getBasicRemote().sendText(broadMessage);
            }
        }


    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
//        example getting unique userID that sent this message
        String userId = session.getId();

        JSONObject jsonmsg = new JSONObject(comm);
        String type = (String) jsonmsg.get("type");
        String message = (String) jsonmsg.get("msg");


        String broadMessage;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.now();

        boolean firstMessage = false;

        if (currentRoom.getUsers().size() > 1)
        {
            System.out.println("Trigger");
        }

        if (currentRoom.getUsers().get(userId).equals(""))
        {
            firstMessage = true;

            currentRoom.setUserName(userId, message);

            String welcomeMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (Server %s): Welcome %s\"}",dtf.format(localTime), currentRoom.getCode(), message);
            String historyMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (Server %s): Here is the chat history\"}",dtf.format(localTime), currentRoom.getCode());
            broadMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (Server %s): %s joined the server\"}",dtf.format(localTime), currentRoom.getCode(), message);


            session.getBasicRemote().sendText(welcomeMessage);
            session.getBasicRemote().sendText(historyMessage);

            if (currentRoom.getUsers().size() > 1)
            {
                for (String record: currentRoom.getChatHistory())
                {
                    session.getBasicRemote().sendText(record);
                }
            }

        }
        else
        {
            broadMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (%s): %s\"}", dtf.format(localTime), currentRoom.getUsers().get(userId), message);
        }

        currentRoom.getChatHistory().add(broadMessage);

        if (firstMessage)
        {
            for (Session peer: session.getOpenSessions())
            {
                if ((peer.getId() != userId ) && (currentRoom.getUsers().get(peer.getId()) != null))
                {
                    peer.getBasicRemote().sendText(broadMessage);
                }
            }
        }
        else
        {
            for (Session peer: session.getOpenSessions())
            {
                if (currentRoom.getUsers().get(peer.getId()) != null)
                {
                    peer.getBasicRemote().sendText(broadMessage);
                }
            }
        }



//        Example conversion of json messages from the client
        //        JSONObject jsonmsg = new JSONObject(comm);
//        String val1 = (String) jsonmsg.get("attribute1");
//        String val2 = (String) jsonmsg.get("attribute2");

        // handle the messages


    }




}