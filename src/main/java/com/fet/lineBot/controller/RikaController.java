package com.fet.lineBot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fet.lineBot.service.MessageService;

@RestController
public class RikaController {

	@Autowired
	MessageService messageService;
	
	@CrossOrigin
	@GetMapping(value = "/list", produces = "application/json")
	public String listAllMessage () {
		String rtnMsg =null;
		rtnMsg = messageService.listMessage();
		return rtnMsg;
	}
}
