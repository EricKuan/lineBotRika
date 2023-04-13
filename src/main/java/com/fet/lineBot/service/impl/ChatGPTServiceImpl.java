package com.fet.lineBot.service.impl;

import com.fet.lineBot.service.ChatGPTService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class ChatGPTServiceImpl implements ChatGPTService {
    @Value("${rikaService.chatGPTKey}")
    private String chatGPTKey;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    private List<com.unfbx.chatgpt.entity.chat.Message> historyMessage = new ArrayList<>();

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
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).build();
            OpenAiClient openAiClient = OpenAiClient.builder().apiKey(Arrays.asList(chatGPTKey)).okHttpClient(okHttpClient).build();
            //聊天模型：gpt-3.5
            com.unfbx.chatgpt.entity.chat.Message chatMessage = com.unfbx.chatgpt.entity.chat.Message.builder().role(com.unfbx.chatgpt.entity.chat.Message.Role.USER).content(message).build();
            List<com.unfbx.chatgpt.entity.chat.Message> inputMessageList = new ArrayList<>();
            inputMessageList.addAll(historyMessage);
            inputMessageList.add(chatMessage);

            ChatCompletion chatCompletion = ChatCompletion.builder().messages(inputMessageList).maxTokens(500).temperature(0.5).build();
            ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
            chatCompletionResponse.getChoices().forEach(e -> {
                log.info(e.getMessage().getContent());
            });
            String content = chatCompletionResponse.getChoices().stream().findFirst().get().getMessage().getContent();
            historyMessage.add(chatCompletionResponse.getChoices().stream().findFirst().get().getMessage());
            if(historyMessage.size()>10){
                historyMessage.remove(0);
            }
            StringBuilder rtnBuffer = new StringBuilder();
            if (StringUtils.isNotBlank(displayName)) {
                rtnBuffer.append("回答 [").append(displayName).append("] \n");
            }
            rtnBuffer.append(content);

            rtnMsg = new TextMessage(rtnBuffer.toString());

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            rtnMsg = new TextMessage("計算逾時或 token 超用");
        }

        return rtnMsg;
    }
}
