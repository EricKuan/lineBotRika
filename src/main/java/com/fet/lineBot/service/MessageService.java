package com.fet.lineBot.service;

public interface MessageService {
	
	public String queryElectionData(String message);
	
	public String queryStickerResponse(String stickId);
	
	public String saveMessageMapping(String message, String replymessage);
	
}
