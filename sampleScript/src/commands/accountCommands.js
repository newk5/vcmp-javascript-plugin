var accountCommands = {
    hasAccess: function (player) {
        if (player.logged != true) {
            server.sendClientMsg(player, new Colour(255, 0, 0), "You need to be registered and logged in to use this command!")
            return false;
        }
        return true;
    },

    commands: [
        {
            name: "stats",
            cmd: function (args) {
              
                var date = args.player.registerDate;

                var formattedDate = DateUtils.formatDate("dd-MM-yyyy HH:mm", date);
                server.sendClientMsg(args.player, new Colour(102, 162, 232), "Kills: " + args.player.kills + " , Joins: " + args.player.joins + ", Registered: " + formattedDate);

                
            }
        }

    ]

}
