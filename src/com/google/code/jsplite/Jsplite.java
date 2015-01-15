package com.google.code.jsplite;

import com.google.code.jsplite.mvc.Controller;
import com.google.code.jsplite.mvc.ModelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * MVC用来向jsp指定哪个控制(controller)类
 *
 * @author lpn
 */
public final class Jsplite {

    /**
     * controller缓存池
     */
    private static Map<String, Controller> pool = new HashMap<String, Controller>();

    /**
     * 为JSP指定哪个controller类
     *
     * @param request
     * @param response
     * @param name
     */
    public static void inherits(String name,
                                HttpServletRequest request, HttpServletResponse response) {
        try {
            //获取controller
            Controller controller = getController(name);

            //执行控制并获取ValueStack
            ModelView valueStack = controller.execute(request, response);

            //向view下发ValueStack
            if (valueStack != null) {
                for (Map.Entry<String, Object> value : valueStack) {
                    request.setAttribute(value.getKey(), value.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从缓存池中获取一个Controller
     */
    private static Controller getController(String name) {
        Controller controller = pool.get(name);

        try {
            if (controller == null) {
                controller = (Controller) Class.forName(name).getConstructor().newInstance();
                pool.put(name, controller);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return controller;
    }

}
