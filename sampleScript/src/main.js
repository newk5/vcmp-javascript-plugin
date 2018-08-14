"use strict"

var http = require("httpclient");
var sql = require("sql");
var irc = require("ircbot");
var fs = require("filesystem");
var crypt = require("crypto");

load("./commands/accountCommands.js");
load("./commands/playerCommands.js");

var conn = null;
var bot = null;

function onServerLoadScripts() {

}


function onServerUnloadScripts() {

}

function onServerShutdown() {

}

function onServerInitialise() {


    //initialise sqlite connection
    this.conn = sql.newConnection("jdbc:sqlite:src/sampleServer.db");

    //initialise mysql connection
    // this.conn = sql.connect("dbName", "localhost", 3306, "dbUser", "password");

    //connect to IRC
    var config = new IRCConfig()
        .setName("guest151234")
        .setServerHostname("irc.liberty-unleashed.co.uk")
        .setServerPort(6667)
        .addAutoJoinChannel("#IRCTest162")
        .addAutoJoinChannel("#IRCTest162Staff channelPassword");

    this.bot = irc.init(config, {
        onConnect: function (msg) {
            console.success(msg);
        },
        onMessage: function (channel, user, msg) {
            server.sendClientMsg(null, new Colour(93, 193, 89), "(IRC) " + user.nick + ": " + msg)
        }
    });

    var teamId = 6;
    var col = new Colour(255, 0, 0);
    var modelId = 73;
    var x = 0;
    var y = 0;
    var z = 0;
    var angle = 0;
    var wep1 = 19;
    var wep2 = 18;
    var wep3 = 21;
    var ammo = 999;

    server.addClass(teamId, col, modelId, x, y, z, angle, wep1, ammo, wep2, ammo, wep3, ammo);
    server.addClass(7, new Colour(100, 50, 20), 74, x, y, z, angle, wep1, ammo, wep2, ammo, wep3, ammo);

    //add command controllers to the command registry
    cmdRegistry.add(accountCommands);
    cmdRegistry.add(playerCommands);

    //set server options
    server.setOption(ServerOption.FrameLimiter, false);

}




function onPlayerSpawn(player) {

}


function onPlayerConnect(player) {

    var stm = conn.instance.prepareStatement("SELECT * FROM Players where nickname=?");
    stm.setString(1, player.name);

    var account = conn.findFirst(stm);
    if (account != null) {
        if (account.ip == player.IP) {
            server.sendClientMsg(player, new Colour(102, 162, 232), "Auto-logged in account: " + account.nickname + ", Level: [ " + account.level + " ]");

            //attach new properties to the player instance: level, kills, joins, accountID, registerDate, logged
            player.level = account.level;
            player.kills = account.kills;
            player.joins = account.joins + 1;
            player.accountID = account.id;
            player.registerDate = account.registerDate;
            //attach a "logged" property, this can be used later to check if the player is logged in
            player.logged = true;

            var date = Date.now();

            //update lastActive date and ip and increment joins
            var updateQ = conn.instance.prepareStatement("UPDATE Players SET lastActive=?, joins=?, ip=? WHERE id=?");
            updateQ.setTimestamp(1, DateUtils.toSQLTimestamp(date));
            updateQ.setInt(2, player.joins);
            updateQ.setString(3, player.IP);
            updateQ.setInt(4, account.id);

            conn.query(updateQ, true);


        } else {
            server.sendClientMsg(player, new Colour(255, 0, 0), "This is a registered account, please /login within the next 30 seconds or you'll be kicked");
            Timers.create(false, 30000, function (player) {
                //when accessing player instances inside timers always check if the player is valid first
                if (player.isValid()) {
                    if (player.logged == undefined || player.logged == null || player.logged == false) {
                        server.sendClientMsg(null, new Colour(255, 0, 0), player.name + " kicked (Login-timeout)");
                        server.kick(player);
                    }

                }
            }, player);
        }
    }




    var red = irc.color("RED");
    this.bot.echo("#IRCTest162", red + player.name + " has joined the server");
}


function onPlayerDisconnect(player, reason) {

}


function onPlayerEnterVehicle(player, vehicle, slot) {

}


function onPlayerExitVehicle(player, vehicle) {

}


function onVehicleExplode(vehicle) {

}


function onPlayerCommand(player, message) {

    //you can parse the command inputs yourself if you want to
    if (message == "heli") {
        console.log("creating heli");
        var v = server.createVehicle(218, player.world, player.pos, 2.1, new VehicleColours(1, 1));
        player.putInVehicle(v, 0, true, false);
    }

    //or you can use the command registry
    cmdRegistry.process(player, message);


}


function onPlayerCrashReport(player, crashLog) {

}


function onCheckPointExited(checkPoint, player) {

}


function onCheckPointEntered(checkPoint, player) {

}


function onPickupRespawn(pickup) {

}


function onPickupPicked(pickup, player) {

}


function onPickupPickAttempt(pickup, player) {


    return true;
}


function onObjectTouched(object, player) {

}


function onObjectShot(object, player, weaponId) {

}


function onVehicleRespawn(vehicle) {

}


function onVehicleUpdate(vehicle, updateType) {

}


function onPlayerSpectate(player, spectated) {

}


function onPlayerKeyBindUp(player, keyBindIndex) {

}


function onPlayerKeyBindDown(player, keyBindIndex) {

}


function onPlayerPrivateMessage(player, recipient, message) {

    return true;
}


function onPlayerMessage(player, message) {
    this.bot.echo("#IRCTest162", irc.color("BLUE") + player.name + ": " + message);

    return true;
}


function onPlayerAwayChange(player, isAway) {

}


function onPlayerEndTyping(player) {


}


function onPlayerBeginTyping(player) {


}


function onPlayerGameKeysChange(player, oldKeys, newKeys) {

}


function onPlayerCrouchChange(player, isCrouching) {

}


function onPlayerOnFireChange(player, isOnFire) {

}


function onPlayerActionChange(player, oldAction, newAction) {

}


function onPlayerStateChange(player, oldState, newState) {

}


function onPlayerNameChange(player, oldName, newName) {

}


function onPlayerRequestEnterVehicle(player, vehicle, slot) {

    return true;

}


function onPlayerDeath(player, killer, reason, bodyPart) {
    if (killer != null & killer != undefined) {
        if (killer.logged) {
            killer.kills++;
            var updateQ = conn.instance.prepareStatement("UPDATE Players SET kills=? WHERE id=?");
            updateQ.setInt(1, killer.kills);
            updateQ.setInt(2, killer.accountID);

            //if you pass true as the second the paramenter instead of a function, this operation will be async too
            conn.query(updateQ, true);
        }
    }
}


function onPlayerRequestSpawn(player) {


    return true;
}


function onPlayerRequestClass(player, classIndex) {

}

function onPlayerModuleList(player, list) {

}


function onClientScriptData(player, stream) {

}


function onPluginCommand(identifier, message) {

}

function onIncomingConnection(name, password, ip) {
    return name;
}

function onServerPerformanceReport(entry, descriptions, times) {

}

/*
    the following 5 events are very CPU intensive, if you're not using them,
    comment them out or just remove them and the server will not process them,
    saving you alot of resources CPU-wise and overall improving server performance
*/
/*
function onPlayerUpdate(player, updateType) {

}

function onPlayerMove(player, oldX, oldY, oldZ, newX, newY, newZ) {

}

function onPlayerHealthChange(player, lastHP, newHP) {

}

function onPlayerArmourChange(player, lastArmour, newArmour) {

}

function onPlayerWeaponChange(player, oldWep, newWep) {

}
*/

