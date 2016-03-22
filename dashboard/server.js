var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var hbase = require('hbase-rpc-client');
var port = process.env.PORT || 3000;

server.listen(port, function() {
  console.log('server listening at port %d', port)
});

var hbaseclient = hbase({
    zookeeperHosts: ['sandbox.hortonworks.com:2181'],
    zookeeperRoot: '/hbase-unsecure',
});

hbaseclient.on('error', function(err) {
    console.log(err)
});


app.get('/test.html', function(req, res){
    res.sendFile(__dirname + '/test.html');
});

app.get('/index.html', function(req, res){
    res.sendFile(__dirname + '/index.html');
});

app.get('/batchdata', function(req, res, next) {
    var result = [];
    var scan = hbaseclient.getScanner('advertising');

    scan.each(function (err, rows) {
        result.push({
            campaign: rows.row.toString(),
            effectiveness: parseFloat(rows.cols['bulk:effectiveness'].value) * 100
        });
    }, function() {
        console.log(result);
        res.json(result);
    });  
});

/*
var get = new hbase.Get('campaign_1');
get.addColumn('bulk');
client.get('advertising', get, function(err, res) {
    //return console.log(arguments);
      var kvs = res.columns;
  for (var i = 0; i < kvs.length; i++) {
    var kv = kvs[i];
    //console.log(Object.getOwnPropertyNames(kv));

    console.log('column: \'%s:%s\', value: \'%s\'', kv.family, kv.qualifier, kv.value);
  }
});*/

//Handle Socket.io connections
io.on('connection', function(socket) {
    console.log('User ' + socket.id + ' connected');    
    socket.on('message', function(data) {
        console.log('got data' + data );
        socket.broadcast.emit('message', data);
    });
});