package com.google.code.zkconfter.web;

import com.google.code.zkconfter.client.ZkConfter;
import com.google.code.jsplite.mvc.Controller;
import com.google.code.jsplite.mvc.ModelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Index implements Controller {

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response,
                        ModelView model) throws Exception {

        ZkConfter zkConfter = new ZkConfter();
        zkConfter.afterPropertiesSet();

        model.addObject("hello", "钩吻");
    }

}
