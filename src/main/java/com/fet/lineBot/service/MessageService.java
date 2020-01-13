package com.fet.lineBot.service;

import com.linecorp.bot.model.message.Message;

public interface MessageService {
	
	public String queryElectionData(String message);
	
	public String queryStickerResponse(String stickId);
	
	public String saveMessageMapping(String message, String replymessage, String senderId);
	
	public Message queryReplyMessage(String message);
	
	public String deleteReplyMessage(String message);
	
	public String listMessage();
	
	public String saveImageMapping(String message, String replyUrl, String senderId);
	
}
