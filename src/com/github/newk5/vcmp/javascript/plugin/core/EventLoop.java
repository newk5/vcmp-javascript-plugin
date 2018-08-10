
package com.github.newk5.vcmp.javascript.plugin.core;

import com.eclipsesource.v8.V8Array;
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
                result.getCallback().release();

            } catch (Exception e) {
                Logger.error(e);
            }
        }

    }

}
