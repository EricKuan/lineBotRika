package com.fet.lineBot.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fet.lineBot.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

	@Value("${rikaService.messagePrefix}")
	private String MESSAGE_PREFIX;
	@Value("${rikaService.stickerId}")
	private String STICKER_ID;

	@Override
	public String queryElectionData(String message) {
		String rtnMsg = null;
		if (0 == message.indexOf(MESSAGE_PREFIX)) {
			rtnMsg = "なに?" + message + "test";
		}

		return rtnMsg;
	}

	@Override
	public String getStickerResponse(String stickId) {
		String rtnMsg = null;
		if(STICKER_ID.equalsIgnoreCase(stickId)) {
			rtnMsg = "気持ちわるっ";
		}
		return rtnMsg;
	}

}
