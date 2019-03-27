/*---------------------------------------------------------------------------------------
--	Source File:	index.js - Starting point of the NodeJS server.
--
--	Date:			March 25, 2019
--
--	Revisions:		(Date and Description)
--
--	Designer:		Daniel Shin, Evan Zhang
--
--	Programmer:		Daniel Shin
--
--	Notes:
--  Starting point of the NodeJS server. The main function of this source file is to
--  create a server and wait for clients to connect. Once a client connects and sends
--  data, the server will connect to the MongoDB database and insert the received data.
---------------------------------------------------------------------------------------*/
var net = require('net');

const MongoClient = require('mongodb').MongoClient;
const mlab_url = 'mongodb://comp4985:team4isthebest@ds017205.mlab.com:17205/comp4985';
const client = new MongoClient(mlab_url);

// Create a server that waits for socket connections
var server = net.createServer(function (socket) {
    console.log("Connected successfully to server");

    socket.write('Echo server\r\n');

    // Data event that occurs when data is received from the socket
    // It calls a callback that sends the data to the database
    socket.on('data', function (data) {
        let received_coordinate = JSON.parse(data);
        console.log('Received: ' + received_coordinate.lat + " " + received_coordinate.lng + " " + received_coordinate.device_id);

        // Connect to the database
        client.connect(function (err) {
            const db = client.db("comp4985");
            const collection_coordinates = db.collection('coordinates');

            // Send data to database
            collection_coordinates.insertOne({
                lat:       received_coordinate.lat,
                lng:       received_coordinate.lng,
                device_id: received_coordinate.device_id,
                time:      received_coordinate.time,
                device_ip: received_coordinate.device_ip
            });
        });
    });
});

server.listen(3000);
