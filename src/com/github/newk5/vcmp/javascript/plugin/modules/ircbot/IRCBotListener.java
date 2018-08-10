package com.github.newk5.vcmp.javascript.plugin.modules.ircbot;

import com.eclipsesource.v8.V8Function;
import com.github.newk5.vcmp.javascript.plugin.core.EventLoop;
import com.github.newk5.vcmp.javascript.plugin.core.common.AsyncResult;
import com.github.newk5.vcmp.javascript.plugin.core.common.CommonResult;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserLevel;
import org.pircbotx.hooks.events.ConnectEvent;


public class IRCBotListener extends ListenerAdapter {

    private EventLoop eventLoop;

    public IRCBotListener(EventLoop eventLoop) {
        this.eventLoop = eventLoop;

    }

    @Override
    public void onConnect(ConnectEvent e) throws Exception {
        String msg =e.getBot().getNick() + " has successfully connected to " + e.getBot().getServerHostname() + ":" + e.getBot().getServerPort();
        V8Function callback = IRCWrapper.getBotWrapper(e.getBot().getNick()).getOnConnectCallback();
        if (callback != null) {
            AsyncResult r = new CommonResult(callback, new Object[]{msg});
            eventLoop.queue.add(r);
        }
    }

    @Override
    public void onMessage(MessageEvent e) throws Exception {

        User u = e.getUser();
        String user = u.getNick();

        //check if the message is not from one of the actual bots so that we dont process it needlessly
        if (!IRCWrapper.botExists(user.toLowerCase())) {

            Channel ch = e.getChannel();
            IRCUser ircUser = new IRCUser(user,
                    hasLevel(u, ch, UserLevel.OP),
                    hasLevel(u, ch, UserLevel.HALFOP),
                    hasLevel(u, ch, UserLevel.SUPEROP),
                    hasLevel(u, ch, UserLevel.VOICE),
                    hasLevel(u, ch, UserLevel.OWNER)
            );

            String input = e.getMessage();
            String channel = ch.getName();

            V8Function callback = IRCWrapper.getCallBack(e.getBot().getNick());
            if (callback != null) {
                AsyncResult r = new CommonResult(callback, new Object[]{channel, ircUser, input});
                eventLoop.queue.add(r);
            }

        }

    }

    private boolean hasLevel(User user, Channel channel, UserLevel l) {
        return user.getUserLevels(channel).contains(l);
    }

}
