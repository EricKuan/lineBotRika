package com.fet.lineBot.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fet.lineBot.domain.dao.MemberDataRepository;
import com.fet.lineBot.domain.model.MemberData;
import com.fet.lineBot.service.MessageService;
import com.google.gson.Gson;

@RestController
public class RikaController {

  private static final Logger logger = LogManager.getLogger(RikaController.class);
  
	@Autowired
	MessageService messageService;
	@Autowired
	MemberDataRepository memberDataRepository;
	
	@CrossOrigin
	@GetMapping(value = "/list", produces = "application/json")
	public String listAllMessage () {
		String rtnMsg =null;
		rtnMsg = messageService.listMessage();
		return rtnMsg;
	}
	
	@CrossOrigin
	@GetMapping(value = "member/list", produces = "application/json")
	public String listAllMember () {
		String rtnMsg =null;
		List<MemberData> allMember = memberDataRepository.findAll();
		rtnMsg = new Gson().toJson(allMember);
		return rtnMsg;
	}
	
	@CrossOrigin
    @GetMapping(value = "/facebook")
    public ResponseEntity<String> facebook (HttpServletRequest request) {
	  logger.info("event: " + new Gson().toJson(request.getAttributeNames()));
	  logger.info("event: " + new Gson().toJson(request.getParameterMap()));
        String rtnMsg =null;
        rtnMsg = "Y";
        return new ResponseEntity<>("Hello World!",  HttpStatus.OK);
    }
	
}
