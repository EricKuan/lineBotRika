package com.fet.lineBot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TemplateController {
	@RequestMapping("/helloHtml") 
	public String hello(Map<String,Object>map) {
		map.put("hello", "from TemplateController.helloHtml");
		List<String> imgList  = new ArrayList<String>();
		imgList.add("https://i.imgur.com/AtgGAta.jpg");
		imgList.add("https://i.imgur.com/U69o62H.jpg");
		map.put("imageList", imgList);
		return "/hello";
		
	}
}
