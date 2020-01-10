package com.fet.lineBot.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fet.lineBot.service.MessageService;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@LineMessageHandler
public class MessageHandler {
	
	@Autowired
	MessageService messageService;
	
	@Value("${rikaService.settingPrefix}")
	private String SETTING_PREFIX;
	
	@EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        System.out.println("event: " + event);
        String message = event.getMessage().getText();
        String rtnMsg;
        /* 設定內容 */
        if(0 == message.indexOf(SETTING_PREFIX)){
        	String[] split = message.split(" ");
        	String[] mapping = split[1].split(",");
        	rtnMsg = messageService.saveMessageMapping(mapping[0], mapping[1]);
        	return new TextMessage(rtnMsg);
        }
        
//        rtnMsg = messageService.queryElectionData(message);
        rtnMsg = messageService.queryReplyMessage(message);
        return new TextMessage(rtnMsg);
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }
    
    @EventMapping
    public TextMessage handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        System.out.println("event: " + event);
        String stickId = event.getMessage().getStickerId();
        String rtnMsg = messageService.queryStickerResponse(stickId);
        return new TextMessage(rtnMsg);
    }
    
}
