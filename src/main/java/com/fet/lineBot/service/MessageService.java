package com.fet.lineBot.service;

public interface MessageService {
	
	public String queryElectionData(String message);
	
	public String queryStickerResponse(String stickId);
	
	public String saveMessageMapping(String message, String replymessage);
	
	public String queryReplyMessage(String message);
	
	public String deleteReplyMessage(String message);
	
	public String listMessage();
	
}
