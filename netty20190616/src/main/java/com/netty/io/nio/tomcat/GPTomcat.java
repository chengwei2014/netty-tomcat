package com.netty.io.nio.tomcat;

import com.netty.io.nio.tomcat.http.GPRequest;
import com.netty.io.nio.tomcat.http.GPResponse;
import com.netty.io.nio.tomcat.http.GPServlet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Tomcat（netty）
 * 2019-07-15
 */
public class GPTomcat {
    //端口
    private int port = 8080;
    //容器，URL和Servlet的映射
    private Map<String,GPServlet> servletMap = new HashMap<String,GPServlet>();
    //配置文件
    private Properties webxml = new Properties();
    /**
     * 初始化
     * */
    public void init(){
        try {
            //加载配置文件
            String WEB_INF = this.getClass().getResource("/").getPath();
            webxml.load(new FileInputStream( WEB_INF + "web.properties"));
            //解析配置文件，URL->Servlet
            for(Object k:webxml.keySet()){
                String key = k.toString();
                if(key.endsWith(".url")){
                    //URL
                    String servletName = key.replaceAll("\\.url$", "");
                    String url = webxml.getProperty(key);
                    //Servlet
                    String className = webxml.getProperty(servletName + ".className");
                    GPServlet servlet = (GPServlet) Class.forName(className).newInstance();
                    servletMap.put(url, servlet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tomcat启动
     * */
    public  void start(){
        //初始化
        init();
        //netty封装了NIO,Reactor模型，Boss,worker
        //boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //worker线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //Netty服务,ServerBootstrap相当于ServerSocketChannel
        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                //主线程处理类
                .channel(NioServerSocketChannel.class)
                //子线程处理类，Handler
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    //客户端初始化处理
                    @Override
                    protected void initChannel(SocketChannel client) throws Exception {
                        //无锁化串行编程
                        //Netty对HTTP协议的封装，顺序有要求
                        client.pipeline().addLast(new HttpResponseEncoder());
                        client.pipeline().addLast(new HttpRequestDecoder());
                        //业务逻辑处理
                        client.pipeline().addLast(new GPTomcatHandler());
                    }
                })
                //针对主线程的配置，分配线程最大数量128
                .option(ChannelOption.SO_BACKLOG,128)
                //针对子线程的配置，保持长连接
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        //启动服务器
        try {
            ChannelFuture future = server.bind(port).sync();
            System.out.println("GP Tomcat已启动，监听的端口是：" + port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //关闭线程池
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public class GPTomcatHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof HttpRequest){
                HttpRequest req = (HttpRequest) msg;
                //转交给我们自己的request实现
                GPRequest request = new GPRequest(ctx, req);
                //转交给我们自己的response实现
                GPResponse response = new GPResponse(ctx, req);
                //实际业务处理
                String url = request.getUrl();
                if(servletMap.containsKey(url)){
                    servletMap.get(url).service(request,response);
                }else{
                    response.write("404 - Not Found 20190729");
                }

            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }
    }

    public static void main(String[] args) {
        new GPTomcat().start();
    }
}
