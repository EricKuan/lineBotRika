package com.fet.lineBot.service.impl;

import com.fet.lineBot.service.ChatGPTService;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class ChatGPTServiceImpl implements ChatGPTService {
    @Override
    public Message returnChatGPT(String message) {

        Message rtnMsg;
        try {
            OkHttpClient okHttpClient = new OkHttpClient
                    .Builder()
                    .connectTimeout(40, TimeUnit.SECONDS)//自定义超时时间
                    .writeTimeout(40, TimeUnit.SECONDS)//自定义超时时间
                    .readTimeout(50, TimeUnit.SECONDS)//自定义超时时间
                    .build();
            OpenAiClient openAiClient = OpenAiClient.builder()
                    .apiKey(Arrays.asList("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"))
                    .okHttpClient(okHttpClient)
                    .build();
            //聊天模型：gpt-3.5
            com.unfbx.chatgpt.entity.chat.Message chatMessage = com.unfbx.chatgpt.entity.chat.Message.builder().role(com.unfbx.chatgpt.entity.chat.Message.Role.USER).content("你好啊我的伙伴！").build();
            ChatCompletion chatCompletion = ChatCompletion.builder().messages(Arrays.asList(chatMessage)).build();
            ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
            chatCompletionResponse.getChoices().forEach(e -> {
                log.info(e.getMessage().getContent());
            });
            String content = chatCompletionResponse.getChoices().stream().findFirst().get().getMessage().getContent();

            rtnMsg = new TextMessage(content);
        }catch (Exception e){
            log.error(e);
            e.printStackTrace();
            rtnMsg = new TextMessage("計算逾時或 token 超用");
        }

        return rtnMsg;
    }
}
