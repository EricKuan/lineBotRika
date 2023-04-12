package com.fet.lineBot.service;

import com.linecorp.bot.model.message.Message;

public interface ChatGPTService {
    Message returnChatGPT(String message);
}
