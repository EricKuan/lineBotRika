package com.fet.lineBot.handler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import static java.util.Collections.singletonList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fet.lineBot.service.ClampService;
import com.fet.lineBot.service.MessageService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;

@LineMessageHandler
public class MessageHandler {

	private static final Logger logger = LogManager.getLogger(MessageHandler.class);

	@Autowired
	MessageService messageService;
	@Autowired
	ClampService clampService;

	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Value("${rikaService.helpKeyword}")
	private String HELP_KEYWORD;
	@Value("${rikaService.settingPrefix}")
	private String SETTING_PREFIX;
	@Value("${rikaService.deletePrefix}")
	private String DELETE_PREFIX;
	@Value("${rikaService.vote}")
	private String VOTE;
	@Value("${rikaService.listAllKeyWord}")
	private String ALL_KEYWORD;
	@Value("${rikaService.imageKeyWord}")
	private String IMAGE_KEYWORD;

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		logger.info("event: " + event);
		String message = event.getMessage().getText();
		String rtnMsg;
		/* 設定內容 */
		if (0 == message.indexOf(SETTING_PREFIX)) {
			String[] split = message.split("看到");
			String[] mapping = split[1].split("回");
			rtnMsg = messageService.saveMessageMapping(mapping[0], mapping[1], event.getSource().getSenderId());
			reply(event.getReplyToken(), new TextMessage(rtnMsg));
			return;
		}

		if (0 == message.indexOf(DELETE_PREFIX)) {
			String[] split = message.split("忘記");

			rtnMsg = messageService.deleteReplyMessage(split[1]);
			reply(event.getReplyToken(), new TextMessage(rtnMsg));
			return;
		}

		if (0 == message.indexOf(VOTE)) {
			rtnMsg = clampService.queryVoteResult();
			reply(event.getReplyToken(), new TextMessage(rtnMsg));
			return;
		}
		if (0 == message.indexOf(HELP_KEYWORD)) {
			StringBuffer sb = new StringBuffer();
			sb.append("記住關鍵字:  \n\t六花請記住,看到 [關鍵字] 回 [回應訊息]\n");
			sb.append("忘記關鍵字: \n\t六花請忘記 [關鍵字]\n");
			sb.append("2020總統選舉選票計數：\n\t六花回報計票\n");
			sb.append("列出所有關鍵字:\n\t六花請列出關鍵字\n");
			sb.append("記住回圖: \n\t六花請回圖看到 [關鍵字] 回 [圖片url]");
			rtnMsg = sb.toString();
			reply(event.getReplyToken(), new TextMessage(rtnMsg));
			return;
		}

		if (0 == message.indexOf(IMAGE_KEYWORD)) {
			String[] split = message.split("看到");
			String[] mapping = split[1].split("回");
			rtnMsg = messageService.saveImageMapping(mapping[0], mapping[1], event.getSource().getSenderId());
			reply(event.getReplyToken(), new TextMessage(rtnMsg));
			return;
		}

		if (0 == message.indexOf("bye")) {
			Source source = event.getSource();
			if (source instanceof GroupSource) {
				try {
					reply(event.getReplyToken(), new TextMessage("bye bye!"));
					lineMessagingClient.leaveGroup(((GroupSource) source).getGroupId()).get();
					return;
				} catch (InterruptedException | ExecutionException e) {
					logger.error(e);
				}
			} else if (source instanceof RoomSource) {
				try {
					reply(event.getReplyToken(), new TextMessage("bye bye!"));
					lineMessagingClient.leaveRoom(((RoomSource) source).getRoomId()).get();
					return;
				} catch (InterruptedException | ExecutionException e) {
					logger.error(e);
				}
			}
		}

		Message rtnMsgObj = messageService.queryReplyMessage(message);
		if(rtnMsgObj!=null) {
			reply(event.getReplyToken(), messageService.queryReplyMessage(message));
		}
		return;

	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		logger.info("event: " + event);
	}

	@EventMapping
	public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
		System.out.println("event: " + event);
		String stickId = event.getMessage().getStickerId();
		String rtnMsg = messageService.queryStickerResponse(stickId);
		reply(event.getReplyToken(), new TextMessage(rtnMsg));
		return;
	}

	private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, singletonList(message));
    }
	
	private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        reply(replyToken, messages, false);
    }
	
	private void reply(@NonNull String replyToken, @NonNull List<Message> messages, boolean notificationDisabled) {
		try {
			BotApiResponse apiResponse = lineMessagingClient
					.replyMessage(new ReplyMessage(replyToken, messages, notificationDisabled)).get();
			logger.info("Sent messages: {}", apiResponse);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

}
