package com.fet.lineBot.service;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;

public interface ChatGPTService {
    Message returnChatGPT(MessageEvent<TextMessageContent> event, String message);
}
