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
		imgList.add("http://dingyue.ws.126.net/2019/1218/ddba1d66j00q2ov4f003rc000rs01djm.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/771c4a83j00q2ov4f007pc000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/590bf295j00q2ov4e007cc000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/9f8865cfj00q2ov4e005dc000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/686e4581j00q2ov4e008kc000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/183d695bj00q2ov4f007uc000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/1c634be4j00q2ov4e0071c000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/1e325b48j00q2ov4e007ec000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/252e095cj00q2ov4f007jc000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/cc09bcb0j00q2ov4f0069c000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/fd84f7b2j00q2ov4f006wc000rs0186m.jpg");
		imgList.add("http://dingyue.ws.126.net/2019/1218/aa49e539j00q2ov4e008bc000rs01u6m.jpg");
		map.put("imgList", imgList);
		return "/hello";
		
	}
}
