package com.fet.lineBot.handler;

import static java.util.Collections.singletonList;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.fet.lineBot.domain.dao.MemberDataRepository;
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
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.NonNull;

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

  @EventMapping
  public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    logger.info("event: " + new Gson().toJson(event));
    String message = event.getMessage().getText();
    String rtnMsg;
    /* 設定內容 */
    if (0 == message.indexOf(SETTING_PREFIX) && event.getSource() instanceof UserSource) {
      String[] split = message.split("看到");
      String[] mapping = split[1].split("回");

      rtnMsg = messageService.saveMessageMapping(mapping[0], mapping[1],
          event.getSource().getSenderId());
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
      StringBuffer sb = new StringBuffer();
      sb.append("記住關鍵字:  \n\t@回文字看到 [關鍵字] 回 [回應訊息]\n");
      sb.append("忘記關鍵字: \n\t@忘記 [關鍵字]\n");
      sb.append("列出所有關鍵字:\n\t六花請列出關鍵字\n");
      sb.append("記住回圖: \n\t@回圖看到 [關鍵字] 回 [圖片url]");
      rtnMsg = sb.toString();
      reply(event.getReplyToken(), new TextMessage(rtnMsg));
      return;
    }

    if (0 == message.indexOf(IMAGE_KEYWORD) && event.getSource() instanceof UserSource) {
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
        Image image = null;
        if(StringUtils.isNotBlank(fbPostData.getImgUrl())) {
        	image = Image.builder().url(new URI(fbPostData.getImgUrl())).build();
        }else {
        	image = Image.builder().url(new URI(DEFAULT_IMG_URL)).build();
        }
        Box body = Box.builder().contents(Arrays.asList(new FlexComponent[] {image, content}))
            .layout(FlexLayout.VERTICAL).build();
        URI uri = new URI("https://www.facebook.com/Wishswing/posts/" + fbPostData.getStoryId());
        AltUri altUri = new AltUri(uri);
        URIAction action = new URIAction("see more", uri, altUri);
        Bubble bubble = Bubble.builder().body(body).action(action).build();
        FlexMessage flexMessage = FlexMessage.builder().altText("FB最新貼文").contents(bubble).build();
        BotApiResponse apiResponse = lineMessagingClient
            .replyMessage(new ReplyMessage(event.getReplyToken(), flexMessage, false)).get();
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
        Box body = Box.builder().contents(Arrays.asList(new FlexComponent[] {image, content}))
            .layout(FlexLayout.VERTICAL).build();
        URI uri = new URI("https://www.facebook.com/Wishswing/posts/" + fbPostData.getStoryId());
        AltUri altUri = new AltUri(uri);
        URIAction action = new URIAction("see more", uri, altUri);
        Bubble bubble = Bubble.builder().body(body).action(action).build();
        FlexMessage flexMessage = FlexMessage.builder().altText("漫畫更新最新回").contents(bubble).build();
        BotApiResponse apiResponse = lineMessagingClient
            .replyMessage(new ReplyMessage(event.getReplyToken(), flexMessage, false)).get();
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
        Box body = Box.builder().contents(Arrays.asList(new FlexComponent[]{image,content})).layout(FlexLayout.VERTICAL).build();
        URI uri = new URI("https://linebotrika.herokuapp.com/hello/39");
        AltUri altUri = new AltUri(uri);
        URIAction action = new URIAction("see more", uri, altUri);
        
        Text content2 = Text.builder().text("看玻璃的另一側最新回").build();
        Image image2 = Image.builder().url(new URI("https://i.imgur.com/0il5n2M.jpg")).build();
        Box body2 = Box.builder().contents(Arrays.asList(new FlexComponent[]{image2,content2})).layout(FlexLayout.VERTICAL).build();
        URI uri2 = new URI("https://linebotrika.herokuapp.com/hello/0");
        AltUri altUri2 = new AltUri(uri);
        URIAction action2 = new URIAction("see more", uri2, altUri2);

        Bubble bubble = Bubble.builder().body(body).action(action).build();
        Bubble bubble2 = Bubble.builder().body(body2).action(action2).build();
        Carousel carousal = Carousel.builder().contents(Arrays.asList(new Bubble[] {bubble, bubble2})).build(); 
        // FlexMessage flexMessage = new FlexMessage("flextest", bubble);
        FlexMessage flexMessage =
            FlexMessage.builder().altText("玻璃的另一側").contents(carousal).build();
        BotApiResponse apiResponse = lineMessagingClient
            .replyMessage(new ReplyMessage(event.getReplyToken(), flexMessage, false)).get();
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
    
    Message rtnMsgObj = messageService.queryReplyMessage(message);
    if (rtnMsgObj != null) {
      reply(event.getReplyToken(), messageService.queryReplyMessage(message));
    }
    return;

  }

  @EventMapping
  public void handleDefaultMessageEvent(Event event) {
    logger.info("event: " + event);
  }

  @EventMapping
  public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
    System.out.println("event: " + event);
    String stickId = event.getMessage().getStickerId();
    String rtnMsg = messageService.queryStickerResponse(stickId);
    if (StringUtils.isNotBlank(rtnMsg)) {
      reply(event.getReplyToken(), new TextMessage(rtnMsg));
    }
    return;
  }

  private void reply(@NonNull String replyToken, @NonNull Message message) {
    reply(replyToken, singletonList(message));
  }

  private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
    reply(replyToken, messages, false);
  }

  private void reply(@NonNull String replyToken, @NonNull List<Message> messages,
      boolean notificationDisabled) {
    try {
      BotApiResponse apiResponse = lineMessagingClient
          .replyMessage(new ReplyMessage(replyToken, messages, notificationDisabled)).get();
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

}
