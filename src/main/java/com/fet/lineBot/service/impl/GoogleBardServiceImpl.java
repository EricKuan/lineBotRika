package com.fet.lineBot.service.impl;

import com.fet.lineBot.service.GoogleBardService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.pkslow.ai.AIClient;
import com.pkslow.ai.GoogleBardClient;
import com.pkslow.ai.domain.Answer;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Log4j2
public class GoogleBardServiceImpl implements GoogleBardService {
    @Value("${rikaService.chatGPTKey}")
    private String googleBardApiKey;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Override
    public Message returnChatGPT(MessageEvent<TextMessageContent> event, String message) {
        Source source = event.getSource();
        String displayName = null;
        if (source instanceof GroupSource) {
            String groupId = ((GroupSource) source).getGroupId();
            String userId = source.getUserId();
            CompletableFuture<UserProfileResponse> memberProfile = lineMessagingClient.getGroupMemberProfile(groupId, userId);

            try {
                displayName = memberProfile.get().getDisplayName();
            } catch (ExecutionException | InterruptedException e) {
                log.error(e);
                e.printStackTrace();

            }
        }

        Message rtnMsg;
        try {
            // 建立 google API client
            AIClient client = new GoogleBardClient(googleBardApiKey);
            // 輸入問題並取得答案
            Answer answer = client.ask(message);

            log.info("answer: {}", answer.getChosenAnswer());

            // 將答案回應至 line msg 中
            StringBuilder rtnBuffer = new StringBuilder();
            if (StringUtils.isNotBlank(displayName)) {
                rtnBuffer.append("回答 [").append(displayName).append("] \n");
            }
            rtnBuffer.append(answer.getChosenAnswer());

            rtnMsg = new TextMessage(rtnBuffer.toString());

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            rtnMsg = new TextMessage("計算逾時或 token 超用");
        }

        return rtnMsg;
    }
}
