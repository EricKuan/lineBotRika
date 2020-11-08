package com.fet.lineBot.service.impl;

import com.fet.lineBot.domain.dao.BonusPhotoDataRepository;
import com.fet.lineBot.domain.model.BonusPhotoData;
import com.fet.lineBot.service.BonusPhotoService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@Service
@Log4j2
public class BonusPhotoServiceImpl implements BonusPhotoService {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    BonusPhotoDataRepository bonusPhotoDataRepo;

    @Value("${rikaService.voteKeyword}")
    private String VOTE_KEYWORD;

    private String TOTAL="TOTAL";

    @Override
    public void addBonusPhotoVoteData(MessageEvent<TextMessageContent> event, String message) {
        // 查詢全部名單
        if (message.indexOf(TOTAL)==0){
            sendAllNameList(event);
            return;
        }

        //1. 驗證格式
      /*
      	艾拉(袴服.ver)-現實與童話的距離 #投票提名
	    [name]-[pieceName] #投票提名
       */
        String voteStr = message.trim().replace(VOTE_KEYWORD, "");
        String[] pieceData = voteStr.split("-");
        String displayName;
        if (pieceData.length != 2) {
            return;
        }



        try {
            //2. 取得投票人名稱
            String userId = event.getSource().getUserId();
            CompletableFuture<UserProfileResponse> memberProfile = lineMessagingClient.getGroupMemberProfile("Cedfd99b56918652ea9fa037057f3b41d", userId);

            displayName = memberProfile.get().getDisplayName();

            //3. 驗證當月份是否有建立過
            Optional<BonusPhotoData> bonusPhotoDataOp = bonusPhotoDataRepo.findByUserId(userId).stream().filter(item -> {
                Calendar recordDate = Calendar.getInstance();
                Calendar now = Calendar.getInstance();
                recordDate.setTime(item.getCreateDate());
                if (recordDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                    return true;
                }
                return false;
            }).findFirst();

            //3.1 有紀錄則進行更新
            BonusPhotoData bonusPhotoData;
            if (bonusPhotoDataOp.isPresent()) {
                bonusPhotoData = bonusPhotoDataOp.get();
                bonusPhotoData.setPieceName(pieceData[1]);
                bonusPhotoData.setCharacterName(pieceData[0]);
            } else {
                //3.2 無紀錄則直接寫入
                bonusPhotoData = new BonusPhotoData();
                bonusPhotoData.setPieceName(pieceData[1]);
                bonusPhotoData.setCharacterName(pieceData[0]);
                bonusPhotoData.setCreateDate(new Date());
                bonusPhotoData.setUserId(userId);
                bonusPhotoData.setLineName(displayName);
            }
            bonusPhotoDataRepo.save(bonusPhotoData);
            //4. 回傳紀錄結果
            reply(event.getReplyToken(), new TextMessage("已收到提名資訊"));


        } catch (InterruptedException | ExecutionException e) {
            log.error(e);
            e.printStackTrace();
        }
        return;
    }

    @Override
    public void sendAllNameList(MessageEvent<TextMessageContent> event) {
        List<BonusPhotoData> bonusPhotoDataList = bonusPhotoDataRepo.findAll().stream().filter(item -> {
            Calendar recordDate = Calendar.getInstance();
            Calendar now = Calendar.getInstance();
            recordDate.setTime(item.getCreateDate());
            if (recordDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        List<FlexComponent> textList = bonusPhotoDataList.stream().map(item -> {
            StringBuilder str = new StringBuilder();
            str.append(item.getCharacterName())
                    .append(" ")
                    .append(item.getPieceName())
                    .append(" ")
                    .append(item.getLineName());
            Text text = Text.builder().text(str.toString()).build();
            return (FlexComponent) text;
        }).collect(Collectors.toList());

        Box body =
                Box.builder().contents(textList).layout(FlexLayout.VERTICAL).build();
        FlexMessage flexMessage = FlexMessage.builder().altText("投票名單").contents(Bubble.builder().body(body).build()).build();

        reply(event.getReplyToken(), flexMessage);
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        reply(replyToken, messages, false);
    }

    private void reply(
            @NonNull String replyToken, @NonNull List<Message> messages, boolean notificationDisabled) {
        try {
            BotApiResponse apiResponse =
                    lineMessagingClient
                            .replyMessage(new ReplyMessage(replyToken, messages, notificationDisabled))
                            .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
