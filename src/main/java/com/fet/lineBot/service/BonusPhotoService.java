package com.fet.lineBot.service;

import com.fet.lineBot.domain.model.BonusPhotoData;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;

import java.util.List;

public interface BonusPhotoService {

    void addBonusPhotoVoteData(MessageEvent<TextMessageContent> event, String message);

    void sendAllNameList(MessageEvent<TextMessageContent> event);

    List<BonusPhotoData> findBonusPhotoVoteData(int year, int month);
}
