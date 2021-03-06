package com.fet.lineBot.controller;

import com.fet.lineBot.domain.model.BonusPhotoData;
import com.fet.lineBot.domain.model.ClipVideoInfo;
import com.fet.lineBot.service.BonusPhotoService;
import com.fet.lineBot.service.ClampService;
import com.fet.lineBot.service.YoutubeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
public class TemplateController {
  @Autowired ClampService clampService;
  @Autowired
  BonusPhotoService bonusService;
  @Autowired
  YoutubeService youtubeService;

  @RequestMapping("/hello/{storyNum}")
  public String hello(Map<String, Object> map, @PathVariable("storyNum") int storyNum) {
    List<String> imgList = clampService.queryAnotherSide(storyNum);

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

  @RequestMapping("/clipVideo")
  public String getClipVideoPage(Map<String, Object> map) throws GeneralSecurityException, IOException {
    try {
      List<ClipVideoInfo> clipVideoIdList = youtubeService.getClipVideoIdList();
      map.put("clipVideoIdList", clipVideoIdList);
      map.put("youtubeCheck", true);
    } catch(Exception e){
      log.error(e);
      map.put("clipVideoIdList", new ArrayList<String>());
      map.put("youtubeCheck", false);
    }
    return "/clipVideo";
  }

  @RequestMapping("/facebookLogin")
  public String getFacenbookLogin() {
    return "/facebookLogin";
  }

}
