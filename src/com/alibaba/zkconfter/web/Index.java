package com.alibaba.zkconfter.web;

import com.google.code.jsplite.mvc.Controller;
import com.google.code.jsplite.mvc.ModelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Index implements Controller {

	@Override
	public ModelView execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelView model = new ModelView();
		model.addObject("hello", "hello world");
		return model;
	}

}
