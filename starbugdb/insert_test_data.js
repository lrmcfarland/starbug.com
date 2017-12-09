// adds test data
//
// ASSUMES: starbug user is index 2 and already loaded
//
// to run:

var users = JSON.parse(cat("../conf/starbugdb.users.json"));
var conn = new Mongo(); // Mongo(<host:port>)

print('----- starbug database -----');

var starbugdb = conn.getDB('starbug');

starbugdb.auth(users['starbug']['create_args']['user'], users['starbug']['create_args']['pwd']);

print('----- insert data -----');

starbugdb.observations.insert({"name": "Mercury", "type": "planet", "datetime": "2017-12-06T05:00:00-8"});
starbugdb.observations.insert({"name": "Venus", "type": "planet", "datetime": "2017-12-06T06:00:00-8"});

var cursor = starbugdb.observations.find();
while ( cursor.hasNext() ) {
    printjson( cursor.next() );
}
