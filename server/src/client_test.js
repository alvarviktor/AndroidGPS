var net = require("net");

var client = new net.Socket();

client.connect(3000, "18.217.152.46", function () {
    console.log("Connected");

    let data = {
        lat:       49.249766,
        lng:       -123.000632,
        device_id: "test1",
        time:      new Date().toDateString(),
        device_ip: "127.0.0.1"
    };

    client.write(JSON.stringify(data));
});
