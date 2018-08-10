package com.github.newk5.vcmp.javascript.plugin.internals;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class Print implements JavaCallback {

    public Print() {
    }

    @Override
    public Object invoke(V8Object vo, V8Array va) {

        Object o = va.get(0);
        System.out.println(o);
        vo.release();
        va.release();
        return null;

    }
}
