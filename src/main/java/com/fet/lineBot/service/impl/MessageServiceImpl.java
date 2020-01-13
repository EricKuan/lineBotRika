package com.fet.lineBot.service.impl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fet.lineBot.domain.dao.ReplyRepository;
import com.fet.lineBot.domain.model.ReplyMapping;
import com.fet.lineBot.service.MessageService;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

@Service
public class MessageServiceImpl implements MessageService {

	@Value("${rikaService.messagePrefix}")
	private String MESSAGE_PREFIX;
	@Value("${rikaService.stickerId}")
	private String STICKER_ID;
	@Value("${rikaService.messageMaxLength}")
	private int MAX_LENGTH;
	@Value("${rikaService.blockKeyWord}")
	private String BLOCK_KEYWORD;

	@Autowired
	ReplyRepository replyRepository;
	
	
	@Autowired
	DataSource dataSource;

	@Override
	public String queryElectionData(String message) {
		String rtnMsg = "";
		if (0 == message.indexOf(MESSAGE_PREFIX)) {
			rtnMsg = "なに?" + message + "test";
		}

		return rtnMsg;
	}

	@Override
	public String queryStickerResponse(String stickId) {
		String rtnMsg = "";
		if (STICKER_ID.equalsIgnoreCase(stickId)) {
			rtnMsg = "気持ちわるっ";
		}
		return rtnMsg;
	}

	@Override
	public String saveMessageMapping(String message, String replymessage, String senderId) {
		ReplyMapping reply = new ReplyMapping();
		if (StringUtils.isEmpty(message) || StringUtils.isEmpty(replymessage) || message.length() > MAX_LENGTH
				|| replymessage.length() > MAX_LENGTH || BLOCK_KEYWORD.indexOf(message) > 0) {
			return "わかんない";
		}
		reply.setMessage(message);
		reply.setReplyMessage(replymessage);
		reply.setCommitUserID(senderId);
		replyRepository.save(reply);
		return "わかった";
	}

	@Override
	public Message queryReplyMessage(String message) {
		List<ReplyMapping> reply = replyRepository.findByMessage(message);
		ReplyMapping replyMessage =null;
		Message rtnMsg;
		if (reply.size() > 0) {
			replyMessage = reply.get(0);
			switch(replyMessage.getReplyType()) {
				case "Image":
					rtnMsg = new  ImageMessage(replyMessage.getReplyMessage(), replyMessage.getReplyMessage());
					return rtnMsg;
				default:
					rtnMsg = new TextMessage(replyMessage.getReplyMessage());
					return rtnMsg;
			}
		}
		return null;
	}

	@Override
	public String deleteReplyMessage(String message) {
		List<ReplyMapping> replyList = replyRepository.findByMessage(message);
		String replyMessage = null;
		if (replyList.size() > 0) {
			for (ReplyMapping item : replyList) {
				replyRepository.deleteById(Long.valueOf(item.getId()));
			}
			replyMessage = "わかった";
		} else {
			replyMessage = "なに?";
		}
		return replyMessage;

	}

	@Override
	public String listMessage() {
		StringBuffer sb = new StringBuffer();
		List<ReplyMapping> replyList = replyRepository.findAll();
		for (ReplyMapping item : replyList) {
			sb.append(item.getId() + ", ");
			sb.append(item.getMessage() + ", ");
			sb.append(item.getReplyMessage() + ", ");
			sb.append(item.getCommitUserID() + "\n");
		}

		return sb.toString();
	}

	@Override
	public String saveImageMapping(String message, String replyUrl, String senderId) {
		if (StringUtils.isEmpty(message) || StringUtils.isEmpty(replyUrl) || message.length() > MAX_LENGTH
				|| replyUrl.length() > MAX_LENGTH || BLOCK_KEYWORD.indexOf(message) > 0) {
			return "わかんない";
		}
		ReplyMapping reply = new ReplyMapping();
		reply.setMessage(message);
		reply.setReplyMessage(replyUrl);
		reply.setReplyType("Image");
		reply.setCommitUserID(senderId);
		replyRepository.save(reply);
		return "わかった";
	}

}
