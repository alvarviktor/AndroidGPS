var net = require('net');

const MongoClient = require('mongodb').MongoClient;
const mlab_url = 'mongodb://comp4985:team4isthebest@ds017205.mlab.com:17205/comp4985';
const client = new MongoClient(mlab_url);

var server = net.createServer(function (socket) {
    console.log("Connected successfully to server");

    socket.write('Echo server\r\n');

    socket.on('data', function (data) {
        let received_coordinate = JSON.parse(data);
        console.log('Received: ' + received_coordinate.lat + " " + received_coordinate.lng + " " + received_coordinate.device_id);

        client.connect(function (err) {
            const db = client.db("comp4985");
            const collection_coordinates = db.collection('coordinates');

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
