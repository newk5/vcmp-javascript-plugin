var playerCommands = {
    hasAccess: function (player) {
        return true;
    },

    commands: [
        {
            name: "register",
            args: "password",
            cmd: function (args) {
                if (args.password != undefined) {
                    var hashedPass = crypt.SHA256(args.password);


                    var date = Date.now();

                    // RETURN_GENERATED_KEYS is used to return the generated DB ID (only use this if your primary key is an integer and is set to AUTO_INCREMENT)
                    var stm = conn.instance.prepareStatement("INSERT INTO Players(nickname,password,ip,registerDate,lastActive,level) VALUES (?,?,?,?,?,?);", SQLOptions.RETURN_GENERATED_KEYS);
                    stm.setString(1, args.player.name);
                    stm.setString(2, hashedPass);
                    stm.setString(3, args.player.IP);
                    //dates should always be saved as SQL timestamps
                    var timestamp = DateUtils.toSQLTimestamp(date);
                    stm.setTimestamp(4, timestamp);
                    stm.setTimestamp(5, timestamp);
                    stm.setInt(6, 1);

                    //when the table's primary key is an integer and is set to AUTO_INCREMENT, you can pass a function with a parameter that will give you the row ID generated by the DB (accoutnID in this case)
                    conn.query(stm, function (accountID) {

                        /*
                        when inside a callback if you're doing anything with a player instance, always check first
                         if the player is valid because when the callback is returned the player may already have left the server
                        */
                        if (args.player.isValid()) {
                            server.sendClientMsg(args.player, new Colour(255, 0, 0), "pm>> Registered successfully");
                            //attach account data to the player instance
                            args.player.level = 1;
                            args.player.kills = 0;
                            args.player.joins = 1;
                            args.player.logged = true;
                            args.player.registerDate =date;
                            args.player.accountID = accountID;
                        }

                    });


                }
            }
        },
        {
            name: "login",
            args: "password",
            cmd: function (args) {
                if (args.password != undefined) {
                    var hashedPass = crypt.SHA256(args.password);

                    var stm = conn.instance.prepareStatement("SELECT * from Players where nickname=? AND password=?");
                    stm.setString(1, args.player.name);
                    stm.setString(2, hashedPass);


                    var account = conn.findFirst(stm);

                    if (account != null) {
                        server.sendClientMsg(args.player, new Colour(70, 169, 70), "pm>> Logged in successfully");

                        //attach new property "level" to the player instance and save the player's account level there
                        args.player.level = account.level;
                        //do the same for "joins", "kills" and id
                        args.player.kills = account.kills;
                        args.player.joins = account.joins + 1
                        args.player.accountID = account.id;
                        args.player.logged = true;
                        args.player.registerDate = DateUtils.parseSQLDate(account.registerDate);

                        var date = Date.now();

                        //update lastActive date and increment joins
                        var updateQ = conn.instance.prepareStatement("UPDATE Players SET lastActive=?, joins=?, ip=? WHERE id=?");
                        updateQ.setTimestamp(1, DateUtils.toSQLTimestamp(date));
                        updateQ.setInt(2, args.player.joins);
                        updateQ.setString(3, args.player.IP);
                        updateQ.setInt(4, account.id);

                        conn.query(updateQ, true);

                    } else {
                        server.sendClientMsg(args.player, new Colour(255, 0, 0), "pm>> wrong password!");

                    }

                }
            }
        }
    ]

}