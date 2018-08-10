

package com.github.newk5.vcmp.javascript.plugin.modules.ircbot;

import com.eclipsesource.v8.V8Function;
import java.util.Objects;
import org.pircbotx.PircBotX;

public class BotWrapper {

    private PircBotX instance;
    private V8Function onMessageCallback;
    private V8Function onConnectCallback;

    public BotWrapper() {
    }

    public BotWrapper(PircBotX instance, V8Function callback) {
        this.instance = instance;
        this.onMessageCallback = callback;
    }

    public PircBotX getInstance() {
        return instance;
    }

    public void setInstance(PircBotX instance) {
        this.instance = instance;
    }

    public V8Function getOnMessageCallback() {
        return onMessageCallback;
    }

    public void setOnMessageCallback(V8Function onMessageCallback) {
        this.onMessageCallback = onMessageCallback;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.instance);
        hash = 79 * hash + Objects.hashCode(this.onMessageCallback);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BotWrapper other = (BotWrapper) obj;
        if (!Objects.equals(this.instance, other.instance)) {
            return false;
        }
        if (!Objects.equals(this.onMessageCallback, other.onMessageCallback)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BotWrapper{" + "instance=" + instance + ", callback=" + onMessageCallback + '}';
    }

    /**
     * @return the onConnectCallback
     */
    public V8Function getOnConnectCallback() {
        return onConnectCallback;
    }

    /**
     * @param onConnectCallback the onConnectCallback to set
     */
    public void setOnConnectCallback(V8Function onConnectCallback) {
        this.onConnectCallback = onConnectCallback;
    }

}
