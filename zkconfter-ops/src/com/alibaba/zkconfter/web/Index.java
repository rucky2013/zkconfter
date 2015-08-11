package com.alibaba.zkconfter.web;

import com.alibaba.zkconfter.client.ZkConfter;
import com.alibaba.zkconfter.client.util.ZkClient;
import com.alibaba.zkconfter.jsplite.mvc.Controller;
import com.alibaba.zkconfter.jsplite.mvc.ModelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class Index implements Controller {

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response,
                        ModelView model) throws Exception {

        ZkConfter zkConfter = new ZkConfter("zkconfter.properties");
        ZkClient zkClient = zkConfter.getZkClient();

        StringBuilder sb = new StringBuilder();
        List<String> list = zkClient.getChildrenOfFullPathRecursive("/");
        for(String s : list){
            sb.append(s + "<br>");
        }

        model.addObject("hello", sb.toString());
    }

}
