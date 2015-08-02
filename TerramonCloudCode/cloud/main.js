
//Hoyt Dingus 7/22/15
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});
  
//========================Shuffling the array of potential spawns and returning it========================
function shuffle (array) {
  
    var currentIndex = array.length;
    var temporaryValue;
    var randomIndex;
  
    while (0 !== currentIndex) {
  
        randomIndex = Math.floor(Math.random() * currentIndex);
        currentIndex -= 1;
  
        temporaryValue = array[currentIndex];
        array[currentIndex] = array[randomIndex];
        array[randomIndex] = temporaryValue;
  
    }
  
    return array;
  
}
  
//========================Cloud function for Parse Query and Monster Spawn Generation========================
Parse.Cloud.define("getSpawn", function(request, response){
  
    var num = 0;
    var potentialSpawns = [];
    var spawn = null;
    var origLat = request.params.userLocationLat;
    var origLng = request.params.userLocationLng;
    var user = request.params.userPointer;
    var radius = request.params.radius;
  
    //========================Query Parse========================
    var monsterQuery = new Parse.Query("MonsterClass");
    //monsterQuery.equalTo("monsterRarity", request.params.monsterRarity);
    monsterQuery.find({
  
        success: function(results) {
  
            console.log(results);
            var count = results.length;
            var min = 1;
            var max = 99;
  
            for (var i = 0; i < count; ++i) {
  
                for (var j = 0; j < 100; ++j) {
  
                    var obj = results[i];
  
                    if (j < obj.get("monsterRarity")) {
  
                        //console.log("Monster: " + results[i].monsterRarity + "");
                        potentialSpawns[potentialSpawns.length] = results[i];
  
                    } else {
  
                        potentialSpawns[potentialSpawns.length] = null;
  
                    }
  
                };
  
                shuffle(potentialSpawns);
                num = Math.floor(Math.random() * (max - min + 1)) + min;
  
                if(!spawn) {
  
                    spawn = potentialSpawns[num];
  
                }
  
                if (!spawn) {
  
                    potentialSpawns = [];
  
                }
  
            };
  
  
            if(spawn) {
 
                // Convert radius from meters to degrees
                var radiusInDegrees = radius / 1110100;
 
                var u = Math.random();
                var v = Math.random();
                var w = radiusInDegrees * Math.sqrt(u);
                var t = 2 * Math.PI * v;
                var x = w * Math.cos(t);
                var deviationLat = w * Math.sin(t);
 
                // Adjust the x-coordinate for the shrinking of the east-west distances
                var deviationLng = x / Math.cos(origLat);
 
                var newLongitude = deviationLng + origLng;
                var newLatitude = deviationLat + origLat;
 
                var monsterGeopoint = new Parse.GeoPoint(newLatitude, newLongitude);
 
                var monster = new Parse.Object("SpawnClass");
 
                monster.save(
                    {
                        monsterPointer: spawn,
                        monsterLocation: monsterGeopoint,
                        userPointer: user
                    },
                    {
                        success: function(result){
                             
                            console.log("Saved!!!!!!!!!!!!!!!!!!!!!");
 
                    },
                        error: function(highScore, error){
                            console.log("failed to create");
                    }
                }).then(function(savedSpawn){
                     
                    response.success(monster);
 
                });
 
                // monster.set("monsterPointer", spawn, null);
                // monster.set("monsterLocation", monsterGeopoint, null);
                // monster.set("userPointer", user, null);
 
                // monster.save(null, {
                //     success: function(result){
                         
                //         console.log("Saved!!!!!!!!!!!!!!!!!!!!!");
 
                //     },
                //     error: function(highScore, error){
                //         console.log("failed to create");
                //     }
                // });
 
 
            } else {
                 
                response.error("No Spawn");
             
            }
        }, 
  
        error: function(error) {
  
            response.error("Monster Spawn Failed");
  
        }
