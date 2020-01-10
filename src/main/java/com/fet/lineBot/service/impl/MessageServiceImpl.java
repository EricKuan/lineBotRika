package com.fet.lineBot.service.impl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fet.lineBot.domain.dao.ReplyRepository;
import com.fet.lineBot.domain.model.ReplyMapping;
import com.fet.lineBot.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

	@Value("${rikaService.messagePrefix}")
	private String MESSAGE_PREFIX;
	@Value("${rikaService.stickerId}")
	private String STICKER_ID;

	
	@Autowired
	ReplyRepository replyRepository;
	
	@Autowired
	DataSource dataSource;
	
	@Override
	public String queryElectionData(String message) {
		String rtnMsg = null;
		if (0 == message.indexOf(MESSAGE_PREFIX)) {
			rtnMsg = "なに?" + message + "test";
		}

		return rtnMsg;
	}

	@Override
	public String queryStickerResponse(String stickId) {
		String rtnMsg = null;
		if(STICKER_ID.equalsIgnoreCase(stickId)) {
			rtnMsg = "気持ちわるっ";
		}
		return rtnMsg;
	}

	@Override
	public String saveMessageMapping(String message, String replymessage) {
		ReplyMapping reply = new ReplyMapping();
		reply.setMessage(message);
		reply.setReplyMessage(replymessage);
		replyRepository.save(reply);
		return "success";
	}

	@Override
	public String queryReplyMessage(String message) {
		List<ReplyMapping> reply = replyRepository.findByMessage(message);
		String replyMessage = null;
		if(reply.size()>0) {
			replyMessage = reply.get(0).getReplyMessage();
		}
		
		return replyMessage;
	}

}
