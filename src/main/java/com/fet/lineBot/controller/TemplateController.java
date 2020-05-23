package com.fet.lineBot.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TemplateController {
	@RequestMapping("/helloHtml") 
	public String hello(Map<String,Object>map) {
		map.put("hello", "from TemplateController.helloHtml");
		return "/hello";
		
	}
}
