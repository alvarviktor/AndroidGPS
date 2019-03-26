var net = require('net');

const MongoClient = require('mongodb').MongoClient;
const mlab_url = 'mongodb://comp4985:team4isthebest@ds017205.mlab.com:17205/comp4985';
const client = new MongoClient(mlab_url);

client.connect(function (err) {
    const db = client.db("comp4985");
    const collection_coordinates = db.collection('coordinates').find();

    collection_coordinates.each(function(err, item) {
        if(item == null) {
            client.close();
            return;
        }
        console.log(item);
    });
});

console.log("Running NodeJS server");
console.log("Waiting for incoming connections...");
var server = net.createServer(function (socket) {
  console.log(`Client connected to server`);

  socket.on('data', function (data) {
    let received_coordinate = JSON.parse(data.toString());
    console.log(received_coordinate);
  });

  socket.on('close', function(data) {
    console.log(`Client disconnected from server`);
  });

});

server.listen(3000);
