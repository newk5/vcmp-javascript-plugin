

package com.github.newk5.vcmp.javascript.plugin.core.common;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.github.newk5.vcmp.javascript.plugin.core.Context;
import io.alicorn.v8.V8JavaAdapter;
import io.alicorn.v8.V8JavaObjectUtils;



public class CommonResult extends AsyncResult implements ResultBuilder {

    public CommonResult(V8Function callback, Object[] params) {
        super(callback, params);
    }

    public CommonResult() {
    }

    @Override
    public V8Array build() {

        V8Array args = V8JavaObjectUtils.translateJavaArgumentsToJavascript(super.getParams(), Context.v8, V8JavaAdapter.getCacheForRuntime(Context.v8));
        return args;
    }

}
