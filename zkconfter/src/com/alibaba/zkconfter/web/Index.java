package com.alibaba.zkconfter.web;

import com.alibaba.zkconfter.client.ZkConfter;
import com.alibaba.zkconfter.jsplite.mvc.Controller;
import com.alibaba.zkconfter.jsplite.mvc.ModelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Index implements Controller {

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response,
                        ModelView model) throws Exception {

        ZkConfter zkConfter = new ZkConfter("zkconfter.properties");
        zkConfter.afterPropertiesSet();

        model.addObject("hello", "钩吻");
    }

}
