package com.fet.lineBot.service;

import com.linecorp.bot.model.message.Message;

public interface MessageService {

  String queryElectionData(String message);

  String queryStickerResponse(String stickId);

  String saveMessageMapping(String message, String replymessage, String senderId);

  Message queryReplyMessage(String message);

  String deleteReplyMessage(String message);

  String listMessage();

  String saveImageMapping(String message, String replyUrl, String senderId);
}
