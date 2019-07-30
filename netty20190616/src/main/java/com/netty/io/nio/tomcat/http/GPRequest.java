package com.netty.io.nio.tomcat.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Request(netty)
 * 2019-07-15
 */
public class GPRequest {
    private ChannelHandlerContext ctx;
    private HttpRequest req;

    public GPRequest(ChannelHandlerContext ctx, HttpRequest request) {
        this.ctx = ctx;
        this.req = request;
    }

    public String getUrl() {
        return req.uri();
    }

    public String getMethod() {
        return req.method().name();
    }
}
