package com.netty.io.nio.tomcat.http;

import javax.swing.*;
import java.io.IOException;

/**
 * Servlet(netty)
 * 2019-07-15
 */
public abstract class GPServlet {
    public void service(GPRequest request,GPResponse response) throws IOException {
        if("GET".equals(request.getMethod())){
            doGet(request, response);
        }else{
            doPost(request, response);
        }
    }
    public abstract void doGet(GPRequest request,GPResponse response) throws IOException;
    public abstract void doPost(GPRequest request,GPResponse response) throws IOException;
}
