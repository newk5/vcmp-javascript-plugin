var cmdRegistry = {
    controllers: [],
    process: function (player, message) {

        message = message.trim();
        var params = message.split(" ");
        var msg = params[0];

        cmdRegistry.controllers.forEach(controller => {

            controller.commands.forEach(c => {
                if (c.name == msg) {
                    var valid = controller.hasAccess == null ? false : controller.hasAccess(player);
                    if (valid) {
                        var args = null;
                        if (c.args != undefined) {
                            args = c.args.split(" ");
                        }

                        var obj = {player: player};
                        if (args != null && params.length - 1 != args.length) {
                            server.sendClientMessage(player, new Colour(229, 0, 0), 'Usage: /' + c.name + ' ' + c.args);
                        } else if (args == null) {
                            c.cmd(obj);
                        } else {
                            for (var i = 0; i < args.length; i++) {
                                var value = params[i + 1];
                                if (c.mutators != null) {
                                    var mutate = c.mutators[args[i]];
                                    if (mutate != null) {
                                        value = mutate(value);
                                    }
                                }
                                obj[args[i]] = value;
                            }

                            c.cmd(obj);
                        }

                    }
                    return;
                }
            });
        });
    },
    add: function (controller) {
        cmdRegistry.controllers.push(controller);
    }
};