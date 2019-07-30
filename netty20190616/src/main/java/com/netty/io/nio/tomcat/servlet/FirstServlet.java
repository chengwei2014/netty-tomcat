package com.netty.io.nio.tomcat.servlet;


import com.netty.io.nio.tomcat.http.GPRequest;
import com.netty.io.nio.tomcat.http.GPResponse;
import com.netty.io.nio.tomcat.http.GPServlet;

import java.io.IOException;

/**
 * FirstServlet
 * 2019-07-29
 */
public class FirstServlet extends GPServlet {
    @Override
    public void doPost(GPRequest request, GPResponse response) throws IOException {
        response.write("This is First Servlet");
    }

    @Override
    public void doGet(GPRequest request, GPResponse response) throws IOException {
        this.doPost(request, response);
    }

}
