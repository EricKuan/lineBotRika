package com.fet.lineBot.controller;

import com.fet.lineBot.domain.model.BonusPhotoData;
import com.fet.lineBot.service.BonusPhotoService;
import com.fet.lineBot.service.ClampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class TemplateController {
  @Autowired ClampService clampService;
  @Autowired
  BonusPhotoService bonusService;

  @RequestMapping("/hello/{storyNum}")
  public String hello(Map<String, Object> map, @PathVariable("storyNum") int storyNum) {
    List<String> imgList = new ArrayList<String>();
    imgList = clampService.queryAnotherSide(storyNum);

    map.put("imgList", imgList);
    map.put("linkBack", "https://linebotrika.herokuapp.com/hello/" + (storyNum + 1));
    map.put("linkForward", "https://linebotrika.herokuapp.com/hello/" + (storyNum - 1));
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

  @RequestMapping("/voteData/{year}/{month}")
  public String getVoteNameList(Map<String, Object> map, @PathVariable("year") int year, @PathVariable("month") int month){
    List<BonusPhotoData> bonusPhotoVoteData = bonusService.findBonusPhotoVoteData(year, month);
    map.put("voteData", bonusPhotoVoteData);
    return "/voteData";
  }
}
