package com.fet.lineBot.controller;

import com.fet.lineBot.domain.dao.MemberDataRepository;
import com.fet.lineBot.domain.model.CheckYoutubeLiveNotifyData;
import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.domain.model.MemberData;
import com.fet.lineBot.service.ClampService;
import com.fet.lineBot.service.MessageService;
import com.fet.lineBot.service.YoutubeService;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Log4j2
public class RikaController {

    @Autowired
    MessageService messageService;
    @Autowired
    MemberDataRepository memberDataRepository;
    @Autowired
    YoutubeService youtubeService;
    @Autowired
    ClampService clampService;

    @CrossOrigin
    @GetMapping(value = "/list", produces = "application/json")
    public String listAllMessage() {
        String rtnMsg;
        rtnMsg = messageService.listMessage();
        return rtnMsg;
    }

    @CrossOrigin
    @GetMapping(value = "member/list", produces = "application/json")
    public String listAllMember() {
        String rtnMsg;
        List<MemberData> allMember = memberDataRepository.findAll();
        rtnMsg = new Gson().toJson(allMember);
        return rtnMsg;
    }

    @CrossOrigin
    @GetMapping(value = "/facebook")
    public ResponseEntity<String> facebook(HttpServletRequest request) {
        StringBuilder rtnJsonData = new StringBuilder();
        try {
            FBPostData fbPostData = clampService.queryFBNewestPost();
            rtnJsonData.append(new Gson().toJson(fbPostData));
        }catch(Exception e){
            log.error(e);
            rtnJsonData.append(e.getMessage());
        }
        return new ResponseEntity<>(rtnJsonData.toString(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/checkYoutube", produces = "application/json")
    public String checkYoutube(){
        String rtnMsg = null;
        CheckYoutubeLiveNotifyData checkYoutubeLiveNotifyData = youtubeService.scheduleClamYoutubeData();
        rtnMsg = new Gson().toJson(checkYoutubeLiveNotifyData);
        return rtnMsg;
    }

    @CrossOrigin
    @GetMapping(value = "/facebookWebhook")
    public ResponseEntity<String> facebookWebhook(HttpServletRequest request) {
        log.info("event: {}" , new Gson().toJson(request.getAttributeNames()));
        log.info("event: {}" , new Gson().toJson(request.getParameterMap()));
        String challenge = request.getParameterMap().get("hub.challenge")[0];
        log.info(challenge);
        return new ResponseEntity<>(challenge, HttpStatus.OK);
    }
}
