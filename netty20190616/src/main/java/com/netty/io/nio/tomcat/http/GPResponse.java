package com.netty.io.nio.tomcat.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.UnsupportedEncodingException;

/**
 * Response(netty)
 * 2019-07-15
 */
public class GPResponse {
    private ChannelHandlerContext ctx;
    private HttpRequest request;
    public GPResponse(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.request = req;
    }

    public void write(String s) {
        try {
            if(s == null || s.length() == 0){
                return;
            }
            //设置HTTP协议及请求头信息
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(s.getBytes("UTF-8")));
            response.headers().set("Content-Type", "text/html");

            //当前是否支持长连接
//            if(HttpUtil.isKeepAlive(request)){
//                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//            }

            ctx.write(response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            ctx.flush();
            ctx.close();
        }
    }
}
