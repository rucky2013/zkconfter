package com.google.code.jsplite.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * MVC用来向jsp指定的控制类接口
 */
public interface Controller {

    /**
     * 接收客户端的请求
     */
    public ModelView execute(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
