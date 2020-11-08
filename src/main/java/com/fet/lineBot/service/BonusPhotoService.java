package com.fet.lineBot.service;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;

public interface BonusPhotoService {

    void addBonusPhotoVoteData(MessageEvent<TextMessageContent> event, String message);

    void sendAllNameList(MessageEvent<TextMessageContent> event);
}
