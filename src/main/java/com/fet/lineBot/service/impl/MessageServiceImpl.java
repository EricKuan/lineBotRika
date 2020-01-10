package com.fet.lineBot.service.impl;

import org.springframework.stereotype.Service;

import com.fet.lineBot.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

	private static String MESSAGE_PREFIX = "call";

	@Override
	public String queryElectionData(String message) {
		String rtnMsg = null;
		if (0 == message.indexOf(MESSAGE_PREFIX)) {
			rtnMsg = "なに?" + message + "test";
		}

		return rtnMsg;
	}

}
