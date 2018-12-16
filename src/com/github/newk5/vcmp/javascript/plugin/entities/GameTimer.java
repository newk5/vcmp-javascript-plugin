

package com.github.newk5.vcmp.javascript.plugin.entities;

import com.eclipsesource.v8.V8Function;
import com.maxorator.vcmp.java.tools.timers.TimerHandle;


public class GameTimer {

    private TimerHandle handle;
    private long interval;
    private boolean recurring;
    private V8Function callback;
    private Object[] params;

    public GameTimer(TimerHandle handle, long interval, boolean recurring, V8Function callback, Object[] params) {
        this.handle = handle;
        this.interval = interval;
        this.recurring = recurring;
        this.callback = callback;
        this.params = params;
    }

    public void cancel() {
        if (handle != null) {
            handle.cancel();
            callback.release();
            handle = null;
        }
    }
/*
    public void restart() {
        if (handle == null || !handle.isActive()) {
            handle = timerRegistry.register(recurring, interval, () -> {
                if (params == null) {
                    callback.call(null, null);
                } else {
                    V8Array args = V8JavaObjectUtils.translateJavaArgumentsToJavascript(params, Context.v8, V8JavaAdapter.getCacheForRuntime(Context.v8));
                    callback.call(null, args);
                }
            });
        }
    }
*/
    public boolean isActive() {
        if (handle != null) {
            return handle.isActive();
        }
        return false;
    }

    public TimerHandle getHandle() {
        return handle;
    }

    public void setHandle(TimerHandle handle) {
        this.handle = handle;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public V8Function getCallback() {
        return callback;
    }

    public void setCallback(V8Function callback) {
        this.callback = callback;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

}
