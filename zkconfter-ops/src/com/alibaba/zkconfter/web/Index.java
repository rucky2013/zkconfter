package com.alibaba.zkconfter.web;

import com.alibaba.zkconfter.ZkConfter;
import com.alibaba.zkconfter.util.ZkClient;
import com.alibaba.zkconfter.jsplite.mvc.Controller;
import com.alibaba.zkconfter.jsplite.mvc.ModelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class Index implements Controller {

    private ZkConfter zkConfter = new ZkConfter("zkconfter.properties");

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response,
                        ModelView model) throws Exception {

        ZkClient zkClient = zkConfter.getZkClient();

        StringBuilder sb = new StringBuilder();
        List<String> list = zkClient.getChildrenOfFullPathRecursive("/");
        for (String s : list) {
            sb.append(s + "<br>");
        }

        model.addObject("hello", sb.toString());
    }

}
