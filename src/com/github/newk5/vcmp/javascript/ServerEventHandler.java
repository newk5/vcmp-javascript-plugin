package com.github.newk5.vcmp.javascript;

import com.eclipsesource.v8.V8Object;
import com.github.newk5.vcmp.javascript.plugin.core.Context;
import com.eclipsesource.v8.V8ScriptExecutionException;
import com.github.newk5.vcmp.javascript.plugin.core.EventLoop;
import com.google.common.io.LittleEndianDataInputStream;
import com.maxorator.vcmp.java.plugin.integration.RootEventHandler;
import com.maxorator.vcmp.java.plugin.integration.placeable.CheckPoint;
import com.maxorator.vcmp.java.plugin.integration.placeable.GameObject;
import com.maxorator.vcmp.java.plugin.integration.placeable.Pickup;
import com.maxorator.vcmp.java.plugin.integration.player.Player;
import com.maxorator.vcmp.java.plugin.integration.server.Server;
import com.maxorator.vcmp.java.plugin.integration.vehicle.Vehicle;
import com.maxorator.vcmp.java.tools.commands.CommandRegistry;
import com.maxorator.vcmp.java.tools.timers.TimerRegistry;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import org.pmw.tinylog.Logger;

public class ServerEventHandler extends RootEventHandler {

    public static final int COLOUR_YELLOWISH = 0xFFFF5500;

    protected CommandRegistry commandRegistry;
    public static TimerRegistry timerRegistry;

    private EventLoop eventLoop;

    public ServerEventHandler(Server server) {
        super(server);

        this.commandRegistry = new CommandRegistry(server);
        this.timerRegistry = new TimerRegistry();

        this.eventLoop = new EventLoop();
        Context.load(server, eventLoop);
    }

    public static void exception(V8ScriptExecutionException e) {
        String msg = e.getJSMessage();
        if (!msg.equals("TypeError: undefined is not a function")) {
            e.printStackTrace();
            String method = "";
            if (e.getStackTrace().length > 0) {
                method = e.getStackTrace()[e.getStackTrace().length - 1].getMethodName();
            }
            Logger.error("(" + e.getFileName() + ":" + e.getLineNumber() + ":" + e.getStartColumn() + ") " + msg + " :: " + method);
        }

    }

    @Override
    public void onServerPerformanceReport(int entry, String[] descriptions, long[] times) {

    }

    @Override
    public void onServerLoadScripts() {

        try {
            Context.v8.executeJSFunction("onServerLoadScripts");
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }

    }

    @Override
    public void onPlayerModuleList(Player player, String list) {

        Object obj = Context.toJavascript(player);

        try {
            Context.v8.executeJSFunction("onPlayerModuleList", obj, list);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public boolean onServerInitialise() {

        try {
            Context.v8.executeJSFunction("onServerInitialise");
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }

        return true;
    }

    @Override
    public void onServerUnloadScripts() {
        try {
            Context.v8.executeJSFunction("onServerUnloadScripts");
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }

    }

    @Override
    public void onServerFrame() {
        timerRegistry.process();
        eventLoop.process();
    }

    @Override
    public String onIncomingConnection(String name, String password, String ip) {

        Object o = null;
        try {
            o = Context.v8.executeJSFunction("onIncomingConnection", name, password, ip);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
        if (o == null) {
            return name;
        } else {
            if (o instanceof String) {
                return o.toString();
            }
        }
        return name;
    }

    @Override
    public void onPlayerSpawn(Player player) {
        Object obj = Context.toJavascript(player);
        try {
            Context.v8.executeJSFunction("onPlayerSpawn", obj);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }

    }

    @Override
    public void onPlayerConnect(Player player) {
        if (Context.functionExists("onPlayerConnect")) {
            Object obj = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerConnect", obj);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerDisconnect(Player player, int reason) {

        try {
            Context.v8.executeJSFunction("onPlayerDisconnect", Context.toJavascript(player), reason);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public void onPlayerEnterVehicle(Player player, Vehicle vehicle, int slot) {
        Object p = Context.toJavascript(player);
        Object v = Context.toJavascript(vehicle);

        try {
            Context.v8.executeJSFunction("onPlayerEnterVehicle", p, v, slot);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public void onPlayerExitVehicle(Player player, Vehicle vehicle) {
        Object o1 = Context.toJavascript(player);
        Object o2 = Context.toJavascript(vehicle);

        try {
            Context.v8.executeJSFunction("onPlayerExitVehicle", o1, o2);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public void onVehicleExplode(Vehicle vehicle) {
        Object obj = Context.toJavascript(vehicle);

        try {
            Context.v8.executeJSFunction("onVehicleExplode", obj);

        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public boolean onPlayerCommand(Player player, String message) {
        Object o1 = Context.toJavascript(player);

        Object o = null;
        try {
            o = Context.v8.executeJSFunction("onPlayerCommand", o1, message);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }

        if (o != null) {
            Boolean oo = (Boolean) o;
            return oo;
        }
        return false;

    }

    @Override
    public void onPlayerCrashReport(Player player, String crashLog) {
        if (Context.functionExists("onPlayerCrashReport")) {
            try {
                Context.v8.executeJSFunction("onPlayerCrashReport", Context.toJavascript(player), crashLog);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onCheckPointExited(CheckPoint checkPoint, Player player) {

        if (Context.functionExists("onCheckPointExited")) {

            Object o1 = Context.toJavascript(checkPoint);
            Object o2 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onCheckPointExited", o1, o2);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onCheckPointEntered(CheckPoint checkPoint, Player player) {

        if (Context.functionExists("onCheckPointEntered")) {

            Object o1 = Context.toJavascript(checkPoint);
            Object o2 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onCheckPointEntered", o1, o2);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPickupRespawn(Pickup pickup) {
        if (Context.functionExists("onPickupRespawn")) {
            try {
                Context.v8.executeJSFunction("onPickupRespawn", Context.toJavascript(pickup));
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPickupPicked(Pickup pickup, Player player) {

        if (Context.functionExists("onPickupPicked")) {

            Object o1 = Context.toJavascript(pickup);
            Object o2 = Context.toJavascript(player);
            try {
                Context.v8.executeJSFunction("onPickupPicked", o1, o2);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);

            }
        }
    }

    @Override
    public boolean onPickupPickAttempt(Pickup pickup, Player player) {

        if (Context.functionExists("onPickupPickAttempt")) {
            Object o1 = Context.toJavascript(pickup);
            Object o2 = Context.toJavascript(player);
            Object o = null;
            try {
                o = Context.v8.executeJSFunction("onPickupPickAttempt", o1, o2);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
            if (o != null) {
                Boolean oo = (Boolean) o;
                return oo;
            }
        }
        return false;
    }

    @Override
    public void onObjectTouched(GameObject object, Player player) {
        if (Context.functionExists("onObjectTouched")) {
            Object o1 = Context.toJavascript(object);
            Object o2 = Context.toJavascript(player);
            try {
                Context.v8.executeJSFunction("onObjectTouched", o1, o2);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onObjectShot(GameObject object, Player player, int weaponId) {
        Object o1 = Context.toJavascript(object);
        Object o2 = Context.toJavascript(player);

        try {
            Context.v8.executeJSFunction("onObjectShot", o1, o2, weaponId);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public void onVehicleRespawn(Vehicle vehicle) {
        Object o1 = Context.toJavascript(vehicle);

        try {
            Context.v8.executeJSFunction("onVehicleRespawn", o1);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public void onVehicleUpdate(Vehicle vehicle, int updateType) {

        if (Context.functionExists("onVehicleUpdate")) {
            Object o1 = Context.toJavascript(vehicle);

            try {
                Context.v8.executeJSFunction("onVehicleUpdate", o1, updateType);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerSpectate(Player player, Player spectated) {
        Object o1 = Context.toJavascript(player);
        Object o2 = Context.toJavascript(spectated);

        try {
            Context.v8.executeJSFunction("onPlayerSpectate", o1, o2);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public void onPlayerKeyBindUp(Player player, int keyBindIndex) {

        if (Context.functionExists("onPlayerKeyBindUp")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerKeyBindUp", o1, keyBindIndex);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerKeyBindDown(Player player, int keyBindIndex) {
        if (Context.functionExists("onPlayerKeyBindDown")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerKeyBindDown", o1, keyBindIndex);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public boolean onPlayerPrivateMessage(Player player, Player recipient, String message) {

        Object o1 = Context.toJavascript(player);
        Object o2 = Context.toJavascript(recipient);

        try {
            Object o = Context.v8.executeJSFunction("onPlayerPrivateMessage", o1, o2, message);
            if (o != null) {
                Boolean oo = (Boolean) o;
                return oo;
            }
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
        return false;
    }

    @Override
    public boolean onPlayerMessage(Player player, String message) {

        if (Context.functionExists("onPlayerMessage")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerMessage", o1, message);

            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
        return true;
    }

    @Override
    public void onPlayerAwayChange(Player player, boolean isAway) {
        if (Context.functionExists("onPlayerAwayChange")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerAwayChange", o1, isAway);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerEndTyping(Player player) {

        if (Context.functionExists("onPlayerEndTyping")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerEndTyping", o1);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerBeginTyping(Player player) {
        if (Context.functionExists("onPlayerBeginTyping")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerBeginTyping", o1);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerGameKeysChange(Player player, int oldKeys, int newKeys) {
        if (Context.functionExists("onPlayerGameKeysChange")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerGameKeysChange", o1, oldKeys, newKeys);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerCrouchChange(Player player, boolean isCrouching) {
        if (Context.functionExists("onPlayerCrouchChange")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerCrouchChange", o1, isCrouching);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerOnFireChange(Player player, boolean isOnFire) {
        if (Context.functionExists("onPlayerOnFireChange")) {

            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerOnFireChange", o1, isOnFire);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerActionChange(Player player, int oldAction, int newAction) {
        if (Context.functionExists("onPlayerActionChange")) {

            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerActionChange", o1, oldAction, newAction);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerStateChange(Player player, int oldState, int newState) {
        if (Context.functionExists("onPlayerStateChange")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerStateChange", o1, oldState, newState);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerNameChange(Player player, String oldName, String newName) {

        Object o1 = Context.toJavascript(player);

        try {
            Context.v8.executeJSFunction("onPlayerNameChange", o1, oldName, newName);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

    @Override
    public boolean onPlayerRequestEnterVehicle(Player player, Vehicle vehicle, int slot) {
        Object o1 = Context.toJavascript(player);
        Object o2 = Context.toJavascript(vehicle);

        try {
            Context.v8.executeJSFunction("onPlayerRequestEnterVehicle", o1, o2, slot);
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
        return true;
    }

    @Override
    public void onPlayerUpdate(Player player, int updateType) {
        if (Context.functionExists("onPlayerUpdate")) {
            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerUpdate", o1, updateType);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerDeath(Player player, Player killer, int reason, int bodyPart) {

        if (Context.functionExists("onPlayerDeath")) {

            Object o1 = Context.toJavascript(player);
            Object o2 = Context.toJavascript(killer);

            try {
                Context.v8.executeJSFunction("onPlayerDeath", o1, o2, reason, bodyPart);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public boolean onPlayerRequestSpawn(Player player) {
        if (Context.functionExists("onPlayerRequestSpawn")) {

            Object o1 = Context.toJavascript(player);

            try {
                Object o = Context.v8.executeJSFunction("onPlayerRequestSpawn", o1);
                if (o != null) {
                    Boolean oo = (Boolean) o;
                    return oo;
                }
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
        return false;
    }

    @Override
    public boolean onPlayerRequestClass(Player player, int classIndex) {
        if (Context.functionExists("onPlayerRequestClass")) {

            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerRequestClass", o1, classIndex);

            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }

        }
        return true;
    }

    @Override
    public void onClientScriptData(Player player, byte[] data) {
        if (Context.functionExists("onClientScriptData")) {

            Object p = Context.toJavascript(player);

            DataInput input = new LittleEndianDataInputStream(new ByteArrayInputStream(data));

            Object stream = Context.toJavascript(input);

            try {
                Context.v8.executeJSFunction("onClientScriptData", p, stream);

            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPluginCommand(int identifier, String message) {
        if (Context.functionExists("onPluginCommand")) {

            try {
                Context.v8.executeJSFunction("onPluginCommand", identifier, message);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onServerShutdown() {
        try {
            Context.v8.executeJSFunction("onServerShutdown");
        } catch (V8ScriptExecutionException e) {
            this.exception(e);
        }
    }

}
