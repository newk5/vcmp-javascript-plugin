package com.github.newk5.vcmp.javascript.plugin.utils;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import static com.github.newk5.vcmp.javascript.ServerEventHandler.timerRegistry;
import com.github.newk5.vcmp.javascript.plugin.Context;
import com.github.newk5.vcmp.javascript.plugin.entities.GameTimer;
import io.alicorn.v8.V8JavaAdapter;
import io.alicorn.v8.V8JavaObjectUtils;

public class Timers {

    public GameTimer create(Boolean recurring, Double interval, V8Function callback, Object... params) {
        if (callback != null && recurring != null && interval != null && callback != null) {
            return new GameTimer(timerRegistry.register(recurring, interval.longValue(), () -> {
                if (params == null) {
                    callback.call(null, null);
                } else {

                    V8Array args = V8JavaObjectUtils.translateJavaArgumentsToJavascript(params, Context.v8, V8JavaAdapter.getCacheForRuntime(Context.v8));
                    callback.call(null, args);
                }
            }), interval.longValue(), recurring, callback, params);
        }
        return null;
    }

}
