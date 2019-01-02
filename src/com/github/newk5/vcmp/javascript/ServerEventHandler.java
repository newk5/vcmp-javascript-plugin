package com.github.newk5.vcmp.javascript;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.github.newk5.vcmp.javascript.plugin.Context;
import com.eclipsesource.v8.V8ScriptExecutionException;
import com.github.newk5.vcmp.javascript.plugin.Context;
import static com.github.newk5.vcmp.javascript.plugin.internals.Runtime.eventLoop;
import com.github.newk5.vcmp.javascript.plugin.output.Console;
import com.github.newk5.vcmp.javascript.plugin.PlayerUpdateEvents;
import com.github.newk5.vcmp.javascript.plugin.PlayerUpdateEvents;
import com.google.common.io.LittleEndianDataInputStream;
import com.maxorator.vcmp.java.plugin.integration.RootEventHandler;
import com.maxorator.vcmp.java.plugin.integration.placeable.CheckPoint;
import com.maxorator.vcmp.java.plugin.integration.placeable.GameObject;
import com.maxorator.vcmp.java.plugin.integration.placeable.Pickup;
import com.maxorator.vcmp.java.plugin.integration.player.Player;
import com.maxorator.vcmp.java.plugin.integration.server.CoordBlipInfo;
import com.maxorator.vcmp.java.plugin.integration.server.Server;
import com.maxorator.vcmp.java.plugin.integration.vehicle.Vehicle;
import com.maxorator.vcmp.java.tools.commands.CommandRegistry;
import com.maxorator.vcmp.java.tools.timers.TimerRegistry;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.math.BigInteger;
import org.pmw.tinylog.Logger;

public class ServerEventHandler extends RootEventHandler {

    public static final int COLOUR_YELLOWISH = 0xFFFF5500;

    protected CommandRegistry commandRegistry;
    public static TimerRegistry timerRegistry;

    private PlayerUpdateEvents playerUpdateEvents;

    public ServerEventHandler(Server server) {
        super(server);

        this.commandRegistry = new CommandRegistry(server);
        this.timerRegistry = new TimerRegistry();

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

        if (Context.functionExists("onServerPerformanceReport")) {
            V8Array arrDesc = new V8Array(Context.v8);
            for (String s : descriptions) {
                arrDesc.push(s);
            }
            V8Array arrTimes = new V8Array(Context.v8);
            for (long t : times) {
                BigInteger bi = new BigInteger(t + "");
                arrTimes.push(Context.toJavascript(bi));
            }
            try {
                Context.v8.executeJSFunction("onServerPerformanceReport", entry, arrDesc, arrTimes);
                arrDesc.release();
                arrTimes.release();

            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    public void onPlayerWeaponChange(Player p, int oldWep, int newWep) {

        if (Context.functionExists("onPlayerWeaponChange")) {
            Object obj = Context.toJavascript(p);
            try {
                Context.v8.executeJSFunction("onPlayerWeaponChange", obj, oldWep, newWep);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    public void onPlayerMove(Player player, float lastX, float lastY, float lastZ, float newX, float newY, float newZ) {
        if (Context.functionExists("onPlayerMove")) {
            Object obj = Context.toJavascript(player);
            try {
                Context.v8.executeJSFunction("onPlayerMove", obj, lastX, lastY, lastZ, newX, newY, newZ);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    public void onPlayerHealthChange(Player player, float lastHP, float newHP) {
        if (Context.functionExists("onPlayerHealthChange")) {
            Object obj = Context.toJavascript(player);
            try {
                Context.v8.executeJSFunction("onPlayerHealthChange", obj, lastHP, newHP);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    public void onPlayerArmourChange(Player player, float lastArmour, float newArmour) {
        if (Context.functionExists("onPlayerArmourChange")) {
            Object obj = Context.toJavascript(player);
            try {
                Context.v8.executeJSFunction("onPlayerArmourChange", obj, lastArmour, newArmour);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onServerLoadScripts() {

        if (Context.functionExists("onServerLoadScripts")) {
            try {
                Context.v8.executeJSFunction("onServerLoadScripts");
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }

    }

    @Override
    public void onPlayerModuleList(Player player, String list) {

        if (Context.functionExists("onPlayerModuleList")) {

            Object obj = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerModuleList", obj, list);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public boolean onServerInitialise() {
        Context.load(server);
        if (Context.playerUpdateFunctionsExist()) {
            playerUpdateEvents = new PlayerUpdateEvents(this);
        }
        System.out.println("");
        Console.printer.yellow("Javascript plugin loaded");
        System.out.println("");
        if (Context.functionExists("onServerInitialise")) {
            try {
                Context.v8.executeJSFunction("onServerInitialise");
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
        return true;
    }

    @Override
    public void onServerUnloadScripts() {
        if (Context.functionExists("onServerUnloadScripts")) {
            try {
                Context.v8.executeJSFunction("onServerUnloadScripts");
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }

    }

    @Override
    public void onServerFrame() {
        timerRegistry.process();
        eventLoop.process();
    }

    @Override
    public String onIncomingConnection(String name, String password, String ip) {
        if (Context.functionExists("onIncomingConnection")) {

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
                } else if (o instanceof V8Object) {
                    return name;
                }
            }
        }
        return name;
    }

    @Override
    public void onPlayerSpawn(Player player) {
        if (Context.functionExists("onPlayerSpawn")) {
            Object obj = Context.toJavascript(player);
            try {
                Context.v8.executeJSFunction("onPlayerSpawn", obj);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerConnect(Player player) {

        if (Context.functionExists("onPlayerConnect")) {
            if (Context.playerUpdateFunctionsExist()) {
                playerUpdateEvents.connect(player);
            }
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
        if (Context.functionExists("onPlayerDisconnect")) {
            try {
                Context.v8.executeJSFunction("onPlayerDisconnect", Context.toJavascript(player), reason);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerEnterVehicle(Player player, Vehicle vehicle, int slot) {

        if (Context.functionExists("onPlayerEnterVehicle")) {

            Object p = Context.toJavascript(player);
            Object v = Context.toJavascript(vehicle);

            try {
                Context.v8.executeJSFunction("onPlayerEnterVehicle", p, v, slot);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onPlayerExitVehicle(Player player, Vehicle vehicle) {

        if (Context.functionExists("onPlayerExitVehicle")) {

            Object o1 = Context.toJavascript(player);
            Object o2 = Context.toJavascript(vehicle);

            try {
                Context.v8.executeJSFunction("onPlayerExitVehicle", o1, o2);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onVehicleExplode(Vehicle vehicle) {
        if (Context.functionExists("onVehicleExplode")) {
            Object obj = Context.toJavascript(vehicle);

            try {
                Context.v8.executeJSFunction("onVehicleExplode", obj);

            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public boolean onPlayerCommand(Player player, String message) {

        if (Context.functionExists("onPlayerCommand")) {
            Object o1 = Context.toJavascript(player);

            Object o = null;
            try {
                Context.v8.executeJSFunction("onPlayerCommand", o1, message);

            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
        return true;

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
                if (o != null) {
                    if (o instanceof V8Object) {
                        return false;
                    } else {
                        Boolean oo = (Boolean) o;
                        return oo;
                    }
                }
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
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
        if (Context.functionExists("onObjectShot")) {
            Object o1 = Context.toJavascript(object);
            Object o2 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onObjectShot", o1, o2, weaponId);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public void onVehicleRespawn(Vehicle vehicle) {
        if (Context.functionExists("onVehicleRespawn")) {
            Object o1 = Context.toJavascript(vehicle);

            try {
                Context.v8.executeJSFunction("onVehicleRespawn", o1);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
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

        if (Context.functionExists("onPlayerSpectate")) {
            Object o1 = Context.toJavascript(player);
            Object o2 = Context.toJavascript(spectated);

            try {
                Context.v8.executeJSFunction("onPlayerSpectate", o1, o2);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
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
        if (Context.functionExists("onPlayerPrivateMessage")) {

            Object o1 = Context.toJavascript(player);
            Object o2 = Context.toJavascript(recipient);

            try {
                Object o = Context.v8.executeJSFunction("onPlayerPrivateMessage", o1, o2, message);
                if (o != null) {
                    if (o instanceof V8Object) {
                        return false;
                    } else {
                        Boolean oo = (Boolean) o;
                        return oo;
                    }
                }
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
        return false;
    }

    @Override
    public boolean onPlayerMessage(Player player, String message) {

        if (Context.functionExists("onPlayerMessage")) {
            Object o1 = Context.toJavascript(player);

            try {
                Object o = Context.v8.executeJSFunction("onPlayerMessage", o1, message);
                if (o != null) {
                    if (o instanceof V8Object) {
                        return false;
                    } else {
                        Boolean oo = (Boolean) o;
                        return oo;
                    }
                }

            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
        return false;
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

        if (Context.functionExists("onPlayerNameChange")) {

            Object o1 = Context.toJavascript(player);

            try {
                Context.v8.executeJSFunction("onPlayerNameChange", o1, oldName, newName);
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

    @Override
    public boolean onPlayerRequestEnterVehicle(Player player, Vehicle vehicle, int slot) {
        if (Context.functionExists("onPlayerRequestEnterVehicle")) {
            Object o1 = Context.toJavascript(player);
            Object o2 = Context.toJavascript(vehicle);

            try {
                Object o = Context.v8.executeJSFunction("onPlayerRequestEnterVehicle", o1, o2, slot);
                if (o != null) {
                    if (o instanceof V8Object) {
                        return false;
                    } else {
                        Boolean oo = (Boolean) o;
                        return oo;
                    }
                }
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
        return false;
    }

    @Override
    public void onPlayerUpdate(Player player, int updateType) {
        if (Context.functionExists("onPlayerUpdate")) {
            if (Context.playerUpdateFunctionsExist()) {
                playerUpdateEvents.update(player);
            }

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
                    if (o instanceof V8Object) {
                        return false;
                    } else {
                        Boolean oo = (Boolean) o;
                        return oo;
                    }
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
                Object o = Context.v8.executeJSFunction("onPlayerRequestClass", o1, classIndex);
                if (o != null) {
                    if (o instanceof V8Object) {
                        return false;
                    } else {
                        Boolean oo = (Boolean) o;
                        return oo;
                    }
                }

            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }

        }
        return false;
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
        if (Context.functionExists("onServerShutdown")) {
            try {
                Context.v8.executeJSFunction("onServerShutdown");
            } catch (V8ScriptExecutionException e) {
                this.exception(e);
            }
        }
    }

}
