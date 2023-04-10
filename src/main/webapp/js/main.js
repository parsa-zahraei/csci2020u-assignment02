let ws;

let codeList = []

function newRoom(){
    //debugger;
    // calling the ChatServlet to retrieve a new room ID
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'text/plain',
        },
    })
        .then(response => response.text())
        .then(response => enterRoom(response)); // enter the room with the code
}

function joinRoom(){
    let code = document.getElementById('room-code').value;

    code += '\r\n'

    enterRoom(code);

}

//Allow client to send messages to other's in same room
document.getElementById("input").addEventListener("keyup", function (event) {
    if (event.keyCode === 13) {
        let request = {"type":"chat", "msg":event.target.value};
        ws.send(JSON.stringify(request));
        event.target.value = "";
    }
});

//Refresh the list of available rooms for a client
function refreshList(){

   //debugger;

    let code = 'lnkFsPN05v186yks';

    // create the web socket
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/" + code);

    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {

        //debugger;

        console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        updateRoomList(message.message);

    }


}

//Update individual elements of the room list
function updateRoomList(code){

    //debugger;

    let roomList = document.getElementById('room-list')

    let codeSplit = code.split(" ");

    if (codeSplit.length == 1)
    {
        if (!(codeList.includes(code)))
        {
            codeList.push(code);

            let newRoom = '<li id=' + code + '>' + code + '</li>';

            roomList.insertAdjacentHTML('afterend', newRoom);
        }
    }

}

function enterRoom(code){

    document.getElementById("log").value = "";

    // refresh the list of rooms
    //updateRoomList(code);

    // create the web socket
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/"+code);

    document.getElementById("room-id-literal").innerHTML = "Current Room: " + code;

    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {

        debugger;

        console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        let splitMessage = message.message.split(" ");


        //If the message contains "lnkFsPN05v186yks", broadcast creation of new room to all clients
        if (splitMessage.length == 2)
        {
            if (splitMessage[1] == 'lnkFsPN05v186yks')
            {
                updateRoomList(splitMessage[0]);
            }
            else
            {
                // handle message
                document.getElementById("log").value += message.message + "\n";
            }

        }
        else
        {
            // handle message
            document.getElementById("log").value += message.message + "\n";
        }


    }
}




