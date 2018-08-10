package com.github.newk5.vcmp.javascript.plugin.modules.httpclient;

import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.github.newk5.vcmp.javascript.plugin.core.Context;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.console;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.v8;
import com.github.newk5.vcmp.javascript.plugin.core.EventLoop;
import com.github.newk5.vcmp.javascript.plugin.core.common.AsyncResult;
import com.github.newk5.vcmp.javascript.plugin.core.common.CommonResult;
import io.alicorn.v8.V8JavaAdapter;
import io.alicorn.v8.V8JavaObjectUtils;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.pmw.tinylog.Logger;

public class OkHttpWrapper {

    private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private EventLoop eventLoop;

    public static void injectClasses() {
        V8JavaAdapter.injectClass(OkHttpClient.class, v8);
        V8JavaAdapter.injectClass("HttpRequest", Request.class, v8);
        V8JavaAdapter.injectClass("RequestBuilder", Request.Builder.class, v8);
        V8JavaAdapter.injectClass("HttpResponse", Response.class, v8);
        V8JavaAdapter.injectClass("HttpCall", Call.class, v8);
        V8JavaAdapter.injectClass("RequestBody", RequestBody.class, v8);
        V8JavaAdapter.injectClass("MediaType", MediaType.class, v8);
        V8JavaAdapter.injectClass("FormBodyBuilder", FormBody.Builder.class, v8);
        V8JavaAdapter.injectClass("MultipartBodyBuilder", MultipartBody.Builder.class, v8);
    }

    public OkHttpWrapper(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    public OkHttpWrapper() {
    }

    public RequestBody asJsonBody(String json) {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
    }

    public RequestBody asPlainTextBody(String str) {
        return RequestBody.create(MediaType.parse("text/plain"), str);
    }

    public OkHttpClient.Builder setTimeouts(OkHttpClient.Builder b, Double connectTimeout, Double writeTimeout, Double readTimeout) {
        if (connectTimeout != null) {
            b.connectTimeout(connectTimeout.longValue(), TimeUnit.MILLISECONDS);
        }
        if (writeTimeout != null) {
            b.writeTimeout(writeTimeout.longValue(), TimeUnit.MILLISECONDS);
        }
        if (readTimeout != null) {
            b.readTimeout(readTimeout.longValue(), TimeUnit.MILLISECONDS);
        }

        return b;
    }

    public Builder requestBuilder() {

        return new Request.Builder();
    }

    public OkHttpClient newClient() {
        return new OkHttpClient();
    }

    public Response execute(Call call, Boolean async, V8Function f) throws IOException {
        if (async != null && async) {
            pool.submit(() -> {
                try {
                    Response resp = call.execute();
                    if (f != null && !"".equals(f)) {
                        AsyncResult r = new CommonResult(f, new Object[]{resp});
                        eventLoop.queue.add(r);
                    }
                } catch (IOException ex) {
                    Logger.error(ex);
                    console.error(ex);
                }
            });
        } else {
            Response resp = call.execute();
            return resp;
        }
        return null;

    }

    private void release(V8Object o) {
        if (o != null && !o.isReleased()) {
            o.release();
        }
    }

    public Response exec(V8Object obj) throws IOException {
        Boolean async = obj.getBoolean("async");
        Object callObj = obj.get("call");
        if (callObj != null) {
            Call call = (Call) Context.toJava(Call.class, obj.get("call"), (V8Object) obj.get("call"));

            V8Function onSuccess = (V8Function) obj.get("onSuccess");
            V8Function onError = (V8Function) obj.get("onError");
            if (async != null && async) {

                pool.submit(() -> {
                    try {
                        Response resp = call.execute();
                        if (onSuccess != null) {
                            AsyncResult r = new CommonResult(onSuccess, new Object[]{resp});
                            eventLoop.queue.add(r);
                            release(onError);
                        }
                    } catch (Exception ex) {

                        AsyncResult r = new CommonResult(onError, new Object[]{ex.toString()});
                        eventLoop.queue.add(r);
                        Logger.error(ex);
                        release(onSuccess);
                    }
                });
            } else {
                try {
                    Response resp = call.execute();
                    return resp;
                } catch (Exception ex) {
                    AsyncResult r = new CommonResult(onError, new Object[]{ex.getMessage()});
                    eventLoop.queue.add(r);
                    Logger.error(ex);
                    release(onSuccess);
                }
            }
        }
        return null;

    }

}
