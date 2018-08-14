package com.github.newk5.vcmp.javascript.plugin.modules.ircbot;

import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8ResultUndefined;
import com.github.newk5.vcmp.javascript.plugin.core.Context;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.console;
import com.github.newk5.vcmp.javascript.plugin.core.EventLoop;
import io.alicorn.v8.V8JavaAdapter;
import io.alicorn.v8.annotations.JSIgnore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.pircbotx.Colors;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pmw.tinylog.Logger;

public class IRCWrapper {

    private static Map<String, BotWrapper> bots = new ConcurrentHashMap<>();
    private static CopyOnWriteArrayList<String> botNames = new CopyOnWriteArrayList<>();
    private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private EventLoop eventLoop;

    public IRCWrapper(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @JSIgnore
    public static void injectClasses() {
        V8JavaAdapter.injectClass("IRCBot", PircBotX.class, Context.v8);
        V8JavaAdapter.injectClass("IRCConfig", Builder.class, Context.v8);
        V8JavaAdapter.injectClass("IRCUser", IRCUser.class, Context.v8);
        V8JavaAdapter.injectClass(BasicThreadFactory.class, Context.v8);

    }

    public String color(String c) {
        return Colors.lookup(c);
    }

    public V8Function getCallBack(V8Object m, String functionName) {
        Object callback = m.get(functionName);
        if (callback != null && callback instanceof V8Function) {
            return (V8Function) callback;
        }
        if (callback instanceof V8ResultUndefined) {
            return null;
        }
        return null;
    }

    public void run(Builder b, V8Object events) {

        if (botNames.addIfAbsent(b.getName().toLowerCase())) {
            b.addListener(new IRCBotListener(eventLoop));
            PircBotX bot = new PircBotX(b.buildConfiguration());
            BotWrapper bw = new BotWrapper(bot, getCallBack(events, "onMessage"));
            bw.setOnConnectCallback(getCallBack(events, "onConnect"));
            bots.put(b.getName(), bw);

            pool.submit(() -> {

                try {
                    bot.startBot();
                } catch (Exception ex) {
                    Logger.error(ex);
                    if (ex.getCause() != null) {
                        console.error(ex.getCause().toString());
                    } else {
                        console.error(ex.getMessage());
                    }
                }

            });
        }
    }

    public void echo(String botName, String channel, String msg) {
        pool.submit(() -> {
            PircBotX b = IRCWrapper.get(botName);
            if (b != null && b.isConnected()) {
                b.sendRaw().rawLineNow("PRIVMSG " + channel + " " + msg);
            }
        });

    }

    public static PircBotX get(String botName) {
        BotWrapper bw = bots.get(botName);
        if (bw != null) {
            return bw.getInstance();
        }
        return null;
    }

    @JSIgnore
    public static V8Function getCallBack(String botName) {
        BotWrapper bw = bots.get(botName);
        if (bw != null) {
            return bw.getOnMessageCallback();
        }
        return null;
    }

    @JSIgnore
    public static BotWrapper getBotWrapper(String botName) {
        BotWrapper bw = bots.get(botName);
        return bw;
    }

    public static boolean botExists(String name) {
        return botNames.contains(name);
    }
}
