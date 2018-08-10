package com.github.newk5.vcmp.javascript.plugin.core;

import com.github.newk5.vcmp.javascript.plugin.internals.utils.NpmUtils;
import com.github.newk5.vcmp.javascript.resources.FileLoader;
import com.github.newk5.vcmp.javascript.plugin.internals.Console;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;
import com.github.newk5.vcmp.javascript.plugin.internals.Stream;
import com.github.newk5.vcmp.javascript.plugin.modules.sql.SQLWrapper;
import com.github.newk5.vcmp.javascript.plugin.modules.httpclient.OkHttpWrapper;
import com.maxorator.vcmp.java.plugin.integration.generic.Colour;
import com.maxorator.vcmp.java.plugin.integration.generic.Entity;
import com.maxorator.vcmp.java.plugin.integration.generic.Quaternion;
import com.maxorator.vcmp.java.plugin.integration.generic.Rotation2d;
import com.maxorator.vcmp.java.plugin.integration.generic.Vector;
import com.maxorator.vcmp.java.plugin.integration.placeable.CheckPoint;
import com.maxorator.vcmp.java.plugin.integration.placeable.GameObject;
import com.maxorator.vcmp.java.plugin.integration.placeable.Pickup;
import com.maxorator.vcmp.java.plugin.integration.player.Player;
import com.maxorator.vcmp.java.plugin.integration.server.CoordBlipInfo;
import com.maxorator.vcmp.java.plugin.integration.server.KeyBind;
import com.maxorator.vcmp.java.plugin.integration.server.MapBounds;
import com.maxorator.vcmp.java.plugin.integration.server.Server;
import com.github.newk5.vcmp.javascript.plugin.internals.Timers;
import com.maxorator.vcmp.java.plugin.integration.server.WastedSettings;
import com.maxorator.vcmp.java.plugin.integration.server.WeaponAndAmmo;
import com.maxorator.vcmp.java.plugin.integration.vehicle.HandlingRule;
import com.maxorator.vcmp.java.plugin.integration.vehicle.Vehicle;
import com.maxorator.vcmp.java.plugin.integration.vehicle.VehicleColours;
import com.github.newk5.vcmp.javascript.plugin.internals.Print;
import com.github.newk5.vcmp.javascript.plugin.internals.ServerWrapper;
import com.github.newk5.vcmp.javascript.plugin.modules.crypto.CryptoWrapper;
import com.github.newk5.vcmp.javascript.plugin.modules.filesystem.FileSystem;
import com.github.newk5.vcmp.javascript.plugin.modules.ircbot.IRCWrapper;
import com.google.common.io.LittleEndianDataInputStream;
import com.maxorator.vcmp.java.plugin.integration.AbstractEventHandler;
import com.maxorator.vcmp.java.plugin.integration.placeable.PickupOption;
import com.maxorator.vcmp.java.plugin.integration.player.PlayerImmunity;
import com.maxorator.vcmp.java.plugin.integration.player.PlayerOption;
import com.maxorator.vcmp.java.plugin.integration.server.ServerOption;
import com.maxorator.vcmp.java.plugin.integration.server.WeaponField;
import com.maxorator.vcmp.java.plugin.integration.vehicle.VehicleDamage;
import com.maxorator.vcmp.java.plugin.integration.vehicle.VehicleImmunity;
import com.maxorator.vcmp.java.plugin.integration.vehicle.VehicleOption;
import com.maxorator.vcmp.java.plugin.integration.vehicle.VehiclePanelStatus;
import com.maxorator.vcmp.java.plugin.integration.vehicle.VehicleSyncReason;
import io.alicorn.v8.V8JavaAdapter;
import io.alicorn.v8.V8JavaObjectUtils;
import io.alicorn.v8.annotations.JSIgnore;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.util.stream.Collectors;
import org.pmw.tinylog.Logger;

public class Context {

    public static V8 v8;
    public static Server server;
    private static EventLoop eventLoop;
    public static Console console;

    private static Map<String, Boolean> declaredFunctions = new HashMap<>();

    public static void reload() {
        // server.reloadScript();
        // Context.v8.release();
        V8JavaObjectUtils.releaseV8Resources(v8);
        Context.v8 = null;
        eventLoop.queue.clear();
        Context.load(server, eventLoop);

    }

    public static boolean functionExists(String functioName) {
        return declaredFunctions.getOrDefault(functioName, false);
    }

    @JSIgnore
    public static V8Value toJavascript(Object o) {
        return (V8Value) V8JavaObjectUtils.translateJavaArgumentToJavascript(o, Context.v8, V8JavaAdapter.getCacheForRuntime(Context.v8));
    }

    @JSIgnore
    public static Object toJava(Class klass, Object o, V8Object receiver) {
        return V8JavaObjectUtils.translateJavascriptArgumentToJava(klass, o, receiver, V8JavaAdapter.getCacheForRuntime(Context.v8));
    }

    @JSIgnore
    public static V8Array toJavascriptArgs(Object o) {
        return V8JavaObjectUtils.translateJavaArgumentsToJavascript(new Object[]{o}, Context.v8, V8JavaAdapter.getCacheForRuntime(Context.v8));
    }

    @JSIgnore
    public static void load(Server s, EventLoop eventLoop) {
        try {

            long start = System.currentTimeMillis();

            String baseDir = System.getProperty("user.dir") + "/src/";

            Context.eventLoop = eventLoop;

            Context.server = s;

            Logger.info("V8 Runtime initializing....");
            long startV8 = System.currentTimeMillis();
            Context.v8 = V8.createV8Runtime();
            long endV8 = System.currentTimeMillis();

            Logger.info("V8 Runtime initialized (" + (endV8 - startV8) + "ms)");

            InputStream in = FileLoader.class.getResourceAsStream("jvm-npm.js");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String npm = reader.lines().collect(Collectors.joining("\n"));

            in = FileLoader.class.getResourceAsStream("CommandRegistry.js");
            reader = new BufferedReader(new InputStreamReader(in));
            String cmdRegistry = reader.lines().collect(Collectors.joining("\n"));

            Logger.info("Injecting java classes into javascript context....");
            long startJavaclasses = System.currentTimeMillis();

            console = new Console();
            V8JavaAdapter.injectObject("console", console, v8);
            V8JavaAdapter.injectObject("server", server, v8);

            ServerWrapper sw = new ServerWrapper();
            sw.overrideFunctions(v8);
            V8JavaAdapter.injectObject("_ServerOverride_", sw, v8);

            V8JavaAdapter.injectObject("NPMUtils", new NpmUtils(), v8);
            V8JavaAdapter.injectObject("CryptoWrapper", new CryptoWrapper(eventLoop), v8);
            V8JavaAdapter.injectObject("OkHttpWrapper", new OkHttpWrapper(eventLoop), v8);
            V8JavaAdapter.injectObject("IRCWrapper", new IRCWrapper(eventLoop), v8);
            V8JavaAdapter.injectObject("SQLWrapper", new SQLWrapper(eventLoop), v8);
            V8JavaAdapter.injectObject("FileSystemWrapper", new FileSystem(eventLoop), v8);

            Context.v8.executeVoidScript(npm);
            Context.v8.executeVoidScript(cmdRegistry);
            FileLoader loader = new FileLoader(console);

            loader.rootDir = baseDir;
            Context.v8.add("__dirname", baseDir);
            Context.v8.registerJavaMethod(loader, "load");
            Context.v8.registerJavaMethod(new Print(), "print");

            V8JavaAdapter.injectClass("Date", LocalDateTime.class, v8);
            V8JavaAdapter.injectClass(BigInteger.class, v8);
            V8JavaAdapter.injectClass(Long.class, v8);
            V8JavaAdapter.injectClass(Integer.class, v8);
            V8JavaAdapter.injectClass(File.class, v8);
            V8JavaAdapter.injectClass(Scanner.class, v8);
            V8JavaAdapter.injectClass(Thread.class, v8);
            V8JavaAdapter.injectClass("JavaException", Exception.class, v8);
            V8JavaAdapter.injectClass("Context", Context.class, v8);
            V8JavaAdapter.injectClass("Stream", Stream.class, v8);

            /* MODULES */
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            Class.forName("org.sqlite.JDBC");
            OkHttpWrapper.injectClasses();
            SQLWrapper.injectClasses();
            IRCWrapper.injectClasses();
            V8JavaAdapter.injectClass(DataInput.class, v8);
            V8JavaAdapter.injectClass(LittleEndianDataInputStream.class, v8);

            //----------------generic-------------------//
            V8JavaAdapter.injectClass(Colour.class, v8);
            V8JavaAdapter.injectObject("Timers", new Timers(), v8);
            V8JavaAdapter.injectClass(Entity.class, v8);
            V8JavaAdapter.injectClass(Quaternion.class, v8);
            V8JavaAdapter.injectClass(Rotation2d.class, v8);
            V8JavaAdapter.injectClass(Vector.class, v8);
            //----------------placeable-------------------//
            V8JavaAdapter.injectClass(CheckPoint.class, v8);
            V8JavaAdapter.injectClass(GameObject.class, v8);
            V8JavaAdapter.injectClass(Pickup.class, v8);
            //----------------player-------------------//
            V8JavaAdapter.injectClass(Player.class, v8);
            V8JavaAdapter.injectClass(PlayerImmunity.class, v8);
            V8JavaAdapter.injectEnumOrdinalsIntoGlobalObj(PlayerImmunity.Flag.class, "PlayerImmunity", null, v8);
            V8JavaAdapter.injectEnumOrdinals(PlayerOption.class, v8);
            V8JavaAdapter.injectEnumOrdinals(PickupOption.class, v8);
            //----------------server-------------------//
            V8JavaAdapter.injectClass(CoordBlipInfo.class, v8);
            V8JavaAdapter.injectClass(KeyBind.class, v8);
            V8JavaAdapter.injectClass(MapBounds.class, v8);
            V8JavaAdapter.injectEnumOrdinals(ServerOption.class, v8);
            V8JavaAdapter.injectClass(WastedSettings.class, v8);
            V8JavaAdapter.injectClass(WeaponAndAmmo.class, v8);
            V8JavaAdapter.injectEnumOrdinals(WeaponField.class, v8);
            //----------------vehicle-------------------//
            V8JavaAdapter.injectEnumOrdinals(HandlingRule.class, v8);
            V8JavaAdapter.injectClass(Vehicle.class, v8);
            V8JavaAdapter.injectClass(VehicleColours.class, v8);
            V8JavaAdapter.injectClass(VehicleDamage.class, v8);
            V8JavaAdapter.injectEnumOrdinalsIntoGlobalObj(VehicleDamage.Door.class, "VehicleDamage", "Door", v8);
            V8JavaAdapter.injectEnumOrdinalsIntoGlobalObj(VehicleDamage.DoorStatus.class, "VehicleDamage", "DoorStatus", v8);
            V8JavaAdapter.injectEnumOrdinalsIntoGlobalObj(VehicleDamage.Panel.class, "VehicleDamage", "Panel", v8);
            V8JavaAdapter.injectEnumOrdinalsIntoGlobalObj(VehicleDamage.PanelStatus.class, "VehicleDamage", "PanelStatus", v8);
            V8JavaAdapter.injectEnumOrdinalsIntoGlobalObj(VehicleDamage.Tyre.class, "VehicleDamage", "Tyre", v8);
            V8JavaAdapter.injectEnumOrdinalsIntoGlobalObj(VehicleDamage.TyreStatus.class, "VehicleDamage", "TyreStatus", v8);
            V8JavaAdapter.injectClass(VehicleImmunity.class, v8);
            V8JavaAdapter.injectEnumOrdinalsIntoGlobalObj(VehicleImmunity.Flag.class, "VehicleImmunity", null, v8);
            V8JavaAdapter.injectEnumOrdinals(VehicleOption.class, v8);
            V8JavaAdapter.injectEnumOrdinals(VehiclePanelStatus.class, v8);
            V8JavaAdapter.injectEnumOrdinals(VehicleSyncReason.class, v8);

            long endJavaclasses = System.currentTimeMillis();

            Logger.info("Java classes injected (" + (endJavaclasses - startJavaclasses) + "ms)");

            String content = new String(Files.readAllBytes(Paths.get(baseDir + "/main.js")));
            v8.executeScript(content, "main.js", 0);

            //check if all the server events all declared, use this 
            for (Method m : AbstractEventHandler.class.getMethods()) {
                declaredFunctions.put(m.getName(), !v8.getObject(m.getName()).isUndefined());
            }
            declaredFunctions.remove("wait");
            declaredFunctions.remove("equals");
            declaredFunctions.remove("toString");
            declaredFunctions.remove("hashCode");
            declaredFunctions.remove("getClass");
            declaredFunctions.remove("notify");
            declaredFunctions.remove("notifyAll");

            long end = System.currentTimeMillis();
            System.out.println((end - start) + "ms");
            Logger.info("Javascript context initialized (" + (end - start) + "ms)");

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }
}
