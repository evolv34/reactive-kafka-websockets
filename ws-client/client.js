var WebSocket = require('ws');
var ws = new WebSocket('ws://localhost:9093/produce',{
    headers : {
        token: "AAAA%2FAAA%3DAAAAAAAA"
    }
});

ws.on('open', function open() {
    console.log("sending message ...");
    for(i = 0; i < 100000; i ++) {
        ws.send('{"topic":"hello1","content":"world - ' + i + '"}');
    }
});