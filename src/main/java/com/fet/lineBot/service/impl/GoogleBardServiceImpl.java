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
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Log4j2
public class GoogleBardServiceImpl implements GoogleBardService {
    @Value("${rikaService.chatGPTKey}")
    private String googleBardApiKey;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=";


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
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\"contents\":[{\"parts\":[{\"text\":\"" + message +"\"}]}]}");
            Request request = new Request.Builder()
                .url(API_URL + googleBardApiKey)
                .post(body)
                .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                String text = jsonObject.getJSONArray("candidates").getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text");
                log.info("answer: {}", text);

                // 將答案回應至 line msg 中
                StringBuilder rtnBuffer = new StringBuilder();
                if (StringUtils.isNotBlank(displayName)) {
                    rtnBuffer.append("回答 [").append(displayName).append("] \n");
                }
                rtnBuffer.append(text);

                rtnMsg = new TextMessage(rtnBuffer.toString());
            }
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            rtnMsg = new TextMessage("計算逾時或 token 超用");
        }

        return rtnMsg;
    }
}
