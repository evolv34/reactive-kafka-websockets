var WebSocket = require('ws');
var ws = new WebSocket('ws://localhost:9093/kafka',{
        headers : {
          token: "AAAA%2FAAA%3DAAAAAAAA"
        }
      });