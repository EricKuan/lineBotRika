package com.fet.lineBot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fet.lineBot.service.ClampService;

@Controller
public class TemplateController {
	@Autowired
	ClampService clampService;
	
	@RequestMapping("/hello/{storyNum}")
	public String hello(Map<String, Object> map, @PathVariable("storyNum") int storyNum) {
		List<String> imgList = new ArrayList<String>();
		imgList = clampService.queryAnotherSide(storyNum); 
		
		map.put("imgList", imgList);
		map.put("linkBack","https://linebotrika.herokuapp.com/hello/" + (storyNum+1));
		map.put("linkForward","https://linebotrika.herokuapp.com/hello/" + (storyNum-1));
		return "/hello";

	}
	
	@RequestMapping("/")
    public String index() {
        
        return "/index";

    }
	
//	@RequestMapping("/novel/{novalNum}")
	public String novel(Map<String, Object> map, @PathVariable("novalNum") int novalNum) {
		
		String content = clampService.getNovel(novalNum); 
		
		map.put("content", content);
		return "/novel";

	}
}
