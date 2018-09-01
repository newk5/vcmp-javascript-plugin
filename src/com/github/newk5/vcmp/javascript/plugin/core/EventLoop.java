package com.github.newk5.vcmp.javascript.plugin.core;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8ScriptExecutionException;
import com.github.newk5.vcmp.javascript.ServerEventHandler;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.console;
import com.github.newk5.vcmp.javascript.plugin.core.common.AsyncResult;
import com.github.newk5.vcmp.javascript.plugin.core.common.ResultBuilder;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.pmw.tinylog.Logger;

public class EventLoop {

    public Queue<AsyncResult> queue = new ConcurrentLinkedQueue<>();
    public ConcurrentHashMap<String, ResultBuilder> resultBuilder = new ConcurrentHashMap<>();

    public void process() {
        Iterator<AsyncResult> it = queue.iterator();

        while (it.hasNext()) {
            try {
                AsyncResult result = it.next();
                it.remove();
                V8Array args = result.build();
                result.getCallback().call(null, args);
                if (!result.isMaintainCallback()) {
                    result.getCallback().release();
                }

            } catch (Exception e) {
                Logger.error(e);

                if (e instanceof V8ScriptExecutionException) {
                    ServerEventHandler.exception((V8ScriptExecutionException) e);
                } else {

                    if (e.getCause() != null) {
                        console.error(e.getCause().getMessage());
                    } else {
                        console.error(e.getMessage());
                    }
                }
            }
        }

    }

}
