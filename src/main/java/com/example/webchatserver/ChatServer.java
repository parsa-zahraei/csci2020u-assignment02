package com.example.webchatserver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import jakarta.servlet.annotation.WebServlet;
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

    //Update client's list of available rooms
    private void updateList(Session session) throws IOException {
        for (ChatRoom room: rooms)
        {
            String existRoomString = String.format("{\"type\": \"chat\", \"message\":\"%s\"}",room.getCode());

            session.getBasicRemote().sendText(existRoomString);

        }
    }

    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {

        //The string "lnkFsPN05v186yks" is key that indicates that the client's list of rooms should be refreshed, rather
        //than a new chatRoom be created
        if (roomID.equals("lnkFsPN05v186yks"))
        {
            updateList(session);
        }
        //Without the key, create a new chatRoom
        else
        {

            //Broadcast the creation of a new chatRoom
            for (Session peer: session.getOpenSessions())
            {
                String roomString = String.format("{\"type\": \"chat\", \"message\":\"%s lnkFsPN05v186yks\"}",roomID);

                peer.getBasicRemote().sendText(roomString);
            }


            String userID = session.getId();

            boolean newRoom = true;

            //Date Time Format Data
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

            //Add a new room to the list of exisitng rooms
            if (newRoom)
            {
                currentRoom = new ChatRoom(roomID, userID);

                rooms.add(currentRoom);

                //currentRoom.getChatHistory().add(String.format("{\"type\": \"chat\", \"message\":\"[%s] Room %s created:",dtf.format(localTime), roomID));
            }

            String initMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s](Server %s): Please state your username to begin.\"}",dtf.format(localTime),roomID);

            //Send initial message to client
            session.getBasicRemote().sendText(initMessage);

//        accessing the roomID parameter
            System.out.println(roomID);
        }

    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {

        //Get session ID
        String userId = session.getId();

        String userName = currentRoom.getUsers().get(userId);
        // do things for when the connection closes

        //Date time format data
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.now();

        //Broadcast that user has left the server
        String broadMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (Server %s): %s left the room\"}",dtf.format(localTime), currentRoom.getCode(), userName);

        currentRoom.getChatHistory().add(broadMessage);

        for (Session peer : session.getOpenSessions())
        {
            if ((peer.getId() != userId ) && (currentRoom.getUsers().get(peer.getId()) != null))
            {
                peer.getBasicRemote().sendText(broadMessage);
            }
        }

        //Remove user from current room
        currentRoom.removeUser(userId);

    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
//        example getting unique userID that sent this message
        String userId = session.getId();

        JSONObject jsonmsg = new JSONObject(comm);
        String type = (String) jsonmsg.get("type");
        String message = (String) jsonmsg.get("msg");


        String broadMessage;

        //Date Format Data
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.now();

        boolean firstMessage = false;

        if (currentRoom.getUsers().size() > 1)
        {
            System.out.println("Trigger");
        }

        //If the user is unitialized
        if (currentRoom.getUsers().get(userId).equals(""))
        {
            firstMessage = true;

            currentRoom.setUserName(userId, message);

            String welcomeMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (Server %s): Welcome %s\"}",dtf.format(localTime), currentRoom.getCode(), message);

            broadMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (Server %s): %s joined the server\"}",dtf.format(localTime), currentRoom.getCode(), message);

            //Send welcome message to user
            session.getBasicRemote().sendText(welcomeMessage);

            //Show history of chat to new user
            if (currentRoom.getUsers().size() > 1)
            {
                String historyMessage = String.format("{\"type\": \"chat\", \"message\":\"[%s] (Server %s): Here is the chat history\"}",dtf.format(localTime), currentRoom.getCode());
                session.getBasicRemote().sendText(historyMessage);
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

        //Broadcast appropiate messages to client's in the same room
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