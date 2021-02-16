package com.fet.lineBot.controller;

import com.fet.lineBot.domain.dao.MemberDataRepository;
import com.fet.lineBot.domain.model.CheckYoutubeLiveNotifyData;
import com.fet.lineBot.domain.model.MemberData;
import com.fet.lineBot.service.MessageService;
import com.fet.lineBot.service.YoutubeService;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class RikaController {

    private static final Logger logger = LogManager.getLogger(RikaController.class);

    @Autowired
    MessageService messageService;
    @Autowired
    MemberDataRepository memberDataRepository;
    @Autowired
    YoutubeService youtubeService;

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
        logger.info("event: " + new Gson().toJson(request.getAttributeNames()));
        logger.info("event: " + new Gson().toJson(request.getParameterMap()));
        String challenge = request.getParameterMap().get("hub.challenge")[0];
        logger.info(challenge);
        return new ResponseEntity<>(challenge, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/checkYoutube", produces = "application/json")
    public String checkYoutube(){
        String rtnMsg = null;
        CheckYoutubeLiveNotifyData checkYoutubeLiveNotifyData = youtubeService.scheduleClamYoutubeData();
        rtnMsg = new Gson().toJson(checkYoutubeLiveNotifyData);
        return rtnMsg;
    }
}
