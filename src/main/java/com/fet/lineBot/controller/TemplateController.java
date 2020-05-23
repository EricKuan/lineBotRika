package com.fet.lineBot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TemplateController {
	@RequestMapping("/hello/{storyNum}")
	public String hello(Map<String, Object> map, @PathVariable("storyNum") int storyNum) {
		map.put("hello", "from TemplateController.helloHtml");
		List<String> imgList = new ArrayList<String>();
		if (storyNum == 28) {
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
			map.put("linkBack","https://linebotrika.herokuapp.com/hello/27");
			map.put("linkForward","https://linebotrika.herokuapp.com/hello/29");
		} else if (storyNum == 29) {
			imgList.add("http://dingyue.ws.126.net/2019/1218/dd72f165j00q2ouo6003rc000rs01djm.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/65d08a2dj00q2ouo6009bc000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/49d8df08j00q2ouo6006wc000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/7ceba09fj00q2ouo6006jc000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/e05bc1a4j00q2ouo6007cc000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/0eefc1f7j00q2ouo6009vc000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/67dd8863j00q2ouo600a0c000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/c864b303j00q2ouo60084c000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/6f291511j00q2ouo60080c000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/35890a18j00q2ouo60081c000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/8e10ed1fj00q2ouo70068c000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/f143372ej00q2ouo6007gc000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/fce89b1dj00q2ouo6006gc000rs0186m.jpg");
			imgList.add("http://dingyue.ws.126.net/2019/1218/ab728fc6j00q2ouo60074c000rs0186m.jpg");
			map.put("linkBack","https://linebotrika.herokuapp.com/hello/28");
			map.put("linkForward","https://linebotrika.herokuapp.com/hello/30");
		}

		map.put("imgList", imgList);
		return "/hello";

	}
}
