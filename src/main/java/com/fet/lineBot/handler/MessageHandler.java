package com.fet.lineBot.handler;

import com.fet.lineBot.domain.dao.BonusPhotoDataRepository;
import com.fet.lineBot.domain.dao.MemberDataRepository;
import com.fet.lineBot.domain.model.BonusPhotoData;
import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.service.ClampService;
import com.fet.lineBot.service.MessageService;
import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.action.URIAction.AltUri;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MemberJoinedEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.Template;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@LineMessageHandler
public class MessageHandler {

    private static final Logger logger = LogManager.getLogger(MessageHandler.class);

    @Autowired
    MessageService messageService;
    @Autowired
    ClampService clampService;
    @Autowired
    MemberDataRepository memberDataRepo;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    BonusPhotoDataRepository bonusPhotoDataRepo;

    @Value("${rikaService.helpKeyword}")
    private String HELP_KEYWORD;

    @Value("${rikaService.settingPrefix}")
    private String SETTING_PREFIX;

    @Value("${rikaService.deletePrefix}")
    private String DELETE_PREFIX;

    @Value("${rikaService.vote}")
    private String VOTE;

    @Value("${rikaService.listAllKeyWord}")
    private String ALL_KEYWORD;

    @Value("${rikaService.imageKeyWord}")
    private String IMAGE_KEYWORD;

    @Value("${rikaService.fbNewestPost}")
    private String FB_NEWEST_POST;

    @Value("${rikaService.fbNewestStory}")
    private String FB_NEWEST_STORY;

    @Value("${rikaService.wellcomeMessage}")
    private String WELLCOME_MSG;

    @Value("${rikaService.defaultImgUrl}")
    private String DEFAULT_IMG_URL;

    @Value("${rikaService.menuImgUrl}")
    private String MENU_IMG_URL;

    @Value("${rikaService.packageId}")
    private String PACKAGE_ID;

    @Value("${rikaService.stickerId}")
    private String STICKER_ID;

    @Value("${rikaService.voteKeyworld}")
    private String VOTE_KEYWORD;


    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event)
            throws URISyntaxException {
        logger.info("event: " + new Gson().toJson(event));
        String message = event.getMessage().getText();
        String rtnMsg;
        /* 設定內容 */
        if (0 == message.indexOf(SETTING_PREFIX)) {
            String[] split = message.split("看到");
            String[] mapping = split[1].split("回");

            rtnMsg =
                    messageService.saveMessageMapping(
                            mapping[0], mapping[1], event.getSource().getSenderId());
            reply(event.getReplyToken(), new TextMessage(rtnMsg));
            return;
        }

        if (0 == message.indexOf(DELETE_PREFIX)) {
            String[] split = message.split("忘記");

            rtnMsg = messageService.deleteReplyMessage(split[1]);
            reply(event.getReplyToken(), new TextMessage(rtnMsg));
            return;
        }

        if (0 == message.indexOf(VOTE)) {
            rtnMsg = clampService.queryVoteResult();
            reply(event.getReplyToken(), new TextMessage(rtnMsg));
            return;
        }
        if (0 == message.indexOf(HELP_KEYWORD)) {
            rtnMsg =
                    "記住關鍵字:  \n\t@回文字看到 [關鍵字] 回 [回應訊息]\n"
                            + "忘記關鍵字: \n\t@忘記 [關鍵字]\n"
                            + "列出所有關鍵字:\n\t六花請列出關鍵字\n"
                            + "記住回圖: \n\t@回圖看到 [關鍵字] 回 [圖片url]";
            reply(event.getReplyToken(), new TextMessage(rtnMsg));

            return;
        }

        if (0 == message.indexOf(IMAGE_KEYWORD)) {
            String[] split = message.split("看到");
            String[] mapping = split[1].split("回");
            rtnMsg =
                    messageService.saveImageMapping(mapping[0], mapping[1], event.getSource().getSenderId());
            reply(event.getReplyToken(), new TextMessage(rtnMsg));
            return;
        }

        if (0 == message.indexOf("@bye")) {
            Source source = event.getSource();
            if (source instanceof GroupSource) {
                try {
                    reply(event.getReplyToken(), new TextMessage("bye bye!"));
                    lineMessagingClient.leaveGroup(((GroupSource) source).getGroupId()).get();
                    return;
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e);
                }
            } else if (source instanceof RoomSource) {
                try {
                    reply(event.getReplyToken(), new TextMessage("bye bye!"));
                    lineMessagingClient.leaveRoom(((RoomSource) source).getRoomId()).get();
                    return;
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e);
                }
            }
        }

        if (0 == message.indexOf(FB_NEWEST_POST)) {
            try {
                FBPostData fbPostData = clampService.queryFBNewestPost();
                Text content = Text.builder().text("FB最新貼文").build();
                Image image;
                if (StringUtils.isNotBlank(fbPostData.getImgUrl())) {
                    image = Image.builder().url(new URI(fbPostData.getImgUrl())).build();
                } else {
                    image = Image.builder().url(new URI(DEFAULT_IMG_URL)).build();
                }

                Bubble bubble = buildBoxBodyData(fbPostData, content, image);
                FlexMessage flexMessage = FlexMessage.builder().altText("FB最新貼文").contents(bubble).build();
                BotApiResponse apiResponse =
                        lineMessagingClient
                                .replyMessage(new ReplyMessage(event.getReplyToken(), flexMessage, false))
                                .get();
                logger.info("Sent messages: {}", apiResponse);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                logger.error(e);
                e.printStackTrace();
            }
            return;
        }

        if (0 == message.indexOf(FB_NEWEST_STORY)) {
            try {
                FBPostData fbPostData = clampService.queryFBNewestStoryPost();
                Text content = Text.builder().text("漫畫更新最新回").build();
                Image image = Image.builder().url(new URI(fbPostData.getImgUrl())).build();
                Bubble bubble = buildBoxBodyData(fbPostData, content, image);
                FlexMessage flexMessage = FlexMessage.builder().altText("漫畫更新最新回").contents(bubble).build();
                BotApiResponse apiResponse =
                        lineMessagingClient
                                .replyMessage(new ReplyMessage(event.getReplyToken(), flexMessage, false))
                                .get();
                logger.info("Sent messages: {}", apiResponse);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                logger.error(e);
                e.printStackTrace();
            }
            return;
        }

        if ("@flex".equalsIgnoreCase(message)) {

            try {
                Text content = Text.builder().text("看玻璃的另一側第一回").build();
                Image image = Image.builder().url(new URI("https://i.imgur.com/FHnvGQS.jpg")).build();
                Box body =
                        Box.builder()
                                .contents(Arrays.asList(image, content))
                                .layout(FlexLayout.VERTICAL)
                                .build();
                URI uri = new URI("https://linebotrika.herokuapp.com/hello/39");
                AltUri altUri = new AltUri(uri);
                URIAction action = new URIAction("see more", uri, altUri);

                Text content2 = Text.builder().text("看玻璃的另一側最新回").build();
                Image image2 = Image.builder().url(new URI("https://i.imgur.com/0il5n2M.jpg")).build();
                Box body2 =
                        Box.builder()
                                .contents(Arrays.asList(image2, content2))
                                .layout(FlexLayout.VERTICAL)
                                .build();
                URI uri2 = new URI("https://linebotrika.herokuapp.com/hello/0");
                AltUri altUri2 = new AltUri(uri);
                URIAction action2 = new URIAction("see more", uri2, altUri2);

                Bubble bubble = Bubble.builder().body(body).action(action).build();
                Bubble bubble2 = Bubble.builder().body(body2).action(action2).build();
                Carousel carousal = Carousel.builder().contents(Arrays.asList(bubble, bubble2)).build();
                // FlexMessage flexMessage = new FlexMessage("flextest", bubble);
                FlexMessage flexMessage =
                        FlexMessage.builder().altText("玻璃的另一側").contents(carousal).build();
                BotApiResponse apiResponse =
                        lineMessagingClient
                                .replyMessage(new ReplyMessage(event.getReplyToken(), flexMessage, false))
                                .get();
                logger.info("Sent messages: {}", apiResponse);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (0 == message.indexOf("@歡迎訊息")) {
            logger.info("event: " + new Gson().toJson(event));
            logger.info(event.getReplyToken());
            reply(event.getReplyToken(), new TextMessage(WELLCOME_MSG));
            return;
        }

        if (0 == message.indexOf("@menu")) {
            logger.info("event: " + new Gson().toJson(event));
            String token = event.getReplyToken();
            replyMenuMsg(token);

            return;
        }

        if (message.contains(VOTE_KEYWORD)) {
            countBonusPhotoVote(event, message);
            return;

        }


        Message rtnMsgObj = messageService.queryReplyMessage(message);
        if (rtnMsgObj != null) {
            reply(event.getReplyToken(), messageService.queryReplyMessage(message));
        }
    }

    private boolean countBonusPhotoVote(MessageEvent<TextMessageContent> event, String message) {
        //1. 驗證格式
      /*
      	艾拉(袴服.ver)-現實與童話的距離 #投票提名
	    [name]-[pieceName] #投票提名
       */
        String voteStr = message.trim().replace(VOTE_KEYWORD, "");
        String[] pieceData = voteStr.split("-");
        String displayName;
        if (pieceData.length != 2) {
            return true;
        }
        try {
            //2. 取得投票人名稱
            String userId = event.getSource().getUserId();
            CompletableFuture<UserProfileResponse> memberProfile = lineMessagingClient.getGroupMemberProfile("Cc35c3de51a2697e10290f73e18e02e27", userId);

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
                return (FlexComponent)text;
            }).collect(Collectors.toList());

            Box body =
                    Box.builder().contents(textList).layout(FlexLayout.VERTICAL).build();
            FlexMessage flexMessage = FlexMessage.builder().altText("投票名單").contents(Bubble.builder().body(body).build()).build();

            reply(event.getReplyToken(), flexMessage);


        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
            e.printStackTrace();
        }
        return false;
    }

    private Bubble buildBoxBodyData(FBPostData fbPostData, Text content, Image image)
            throws URISyntaxException {
        Box body =
                Box.builder().contents(Arrays.asList(image, content)).layout(FlexLayout.VERTICAL).build();
        URI uri = new URI("https://www.facebook.com/Wishswing/posts/" + fbPostData.getStoryId());
        AltUri altUri = new AltUri(uri);
        URIAction action = new URIAction("see more", uri, altUri);
        return Bubble.builder().body(body).action(action).build();
    }

    private void replyMenuMsg(String token) throws URISyntaxException {

        FBPostData fbPostData = clampService.queryFBNewestStoryPost();
        URIAction newestStory =
                new URIAction(
                        "漫畫最新回",
                        new URI("https://www.facebook.com/Wishswing/posts/" + fbPostData.getStoryId()),
                        null);

        fbPostData = clampService.queryFBNewestPost();
        URIAction newestPost =
                new URIAction(
                        "最新貼文",
                        new URI("https://www.facebook.com/Wishswing/posts/" + fbPostData.getStoryId()),
                        null);

        URIAction introduction =
                new URIAction(
                        "前導介紹",
                        new URI("http://www.wishstudio.com.tw/2969423526332873146135441303403631738626.html"),
                        null);
        URIAction subscription =
                new URIAction(
                        "訂閱資訊", new URI("http://www.wishstudio.com.tw/97333533038321wish9733.html"), null);

        Template template =
                ButtonsTemplate.builder()
                        .title("MENU")
                        .thumbnailImageUrl(new URI(MENU_IMG_URL))
                        .text("請選擇指令")
                        .actions(Arrays.asList(introduction, newestPost, newestStory, subscription))
                        .build();

        TemplateMessage replyTemplateMsg =
                TemplateMessage.builder().template(template).altText("選單").build();

        reply(token, replyTemplateMsg);
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        logger.info("event: " + event);
    }

    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        System.out.println("event: " + event);
        String stickId = event.getMessage().getStickerId();
        String packageId = event.getMessage().getPackageId();
        if (STICKER_ID.equalsIgnoreCase(stickId) && PACKAGE_ID.equalsIgnoreCase(packageId)) {
            String token = event.getReplyToken();
            try {
                replyMenuMsg(token);
            } catch (Exception e) {
                logger.error(e);
            }
            return;
        }

        /* 處理貼圖回文 */
        String rtnMsg = messageService.queryStickerResponse(stickId);
        if (StringUtils.isNotBlank(rtnMsg)) {
            reply(event.getReplyToken(), new TextMessage(rtnMsg));
        }
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
            logger.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @EventMapping
    public void handleJoinEvent(MemberJoinedEvent event) {

        logger.info("event: " + new Gson().toJson(event));
        if (event.getSource() instanceof GroupSource) {
            GroupSource group = (GroupSource) event.getSource();
            if ("Cedfd99b56918652ea9fa037057f3b41d".equals(group.getGroupId())) {
                logger.info(event.getReplyToken());
                reply(event.getReplyToken(), new TextMessage(WELLCOME_MSG));
            }
        }
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        event.getPostbackContent().getData();
        MessageEvent<TextMessageContent> textMessageContentMessageEvent =
                new MessageEvent(
                        event.getReplyToken(),
                        event.getSource(),
                        new TextMessageContent(null, event.getPostbackContent().getData()),
                        event.getTimestamp());
        try {
            handleTextMessageEvent(textMessageContentMessageEvent);
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
