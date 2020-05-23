package com.fet.lineBot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fet.lineBot.domain.dao.MemberDataRepository;
import com.fet.lineBot.domain.model.MemberData;
import com.fet.lineBot.service.MessageService;
import com.google.gson.Gson;

@RestController
public class RikaController {

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
	
}
