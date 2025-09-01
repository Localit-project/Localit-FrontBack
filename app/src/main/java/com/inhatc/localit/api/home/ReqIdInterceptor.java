package com.inhatc.localit.api.home;

import android.util.Log;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class ReqIdInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        String reqId = System.currentTimeMillis() + "-" + ((int)(Math.random() * 10000));
        Request req = chain.request().newBuilder()
                .header("X-Req-Id", reqId)
                .build();

        long t0 = System.nanoTime();
        Log.i("HTTP", "--> " + req.method() + " " + req.url() + " reqId=" + req.header("X-Req-Id"));

        Response resp = chain.proceed(req);

        long t1 = System.nanoTime();
        Log.i("HTTP", "<-- " + resp.code() + " " + req.url()
                + " reqId=" + req.header("X-Req-Id") + " (" + ((t1 - t0) / 1_000_000d) + "ms)");
        return resp;
    }
}
