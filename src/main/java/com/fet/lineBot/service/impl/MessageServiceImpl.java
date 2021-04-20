package com.fet.lineBot.service.impl;

import com.fet.lineBot.domain.dao.ReplyRepository;
import com.fet.lineBot.domain.model.ReplyMapping;
import com.fet.lineBot.service.MessageService;
import com.google.gson.Gson;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Log4j2
@Service
public class MessageServiceImpl implements MessageService {

  private static final Logger logger = LogManager.getLogger(MessageService.class);

  @Value("${rikaService.messagePrefix}")
  private String MESSAGE_PREFIX;

  @Value("${rikaService.stickerId}")
  private String STICKER_ID;

  @Value("${rikaService.messageMaxLength}")
  private int MAX_LENGTH;

  @Value("${rikaService.blockKeyWord}")
  private String BLOCK_KEYWORD;

  @Autowired ReplyRepository replyRepository;

  @Autowired DataSource dataSource;

  @Override
  public String queryElectionData(String message) {
    String rtnMsg = "";
    if (0 == message.indexOf(MESSAGE_PREFIX)) {
      rtnMsg = "なに?" + message + "test";
    }

    return rtnMsg;
  }

  @Override
  public String queryStickerResponse(String stickId) {
    String rtnMsg = "";
    if (STICKER_ID.equalsIgnoreCase(stickId)) {
      rtnMsg = "気持ちわるっ";
    }
    return rtnMsg;
  }

  @Override
  public String saveMessageMapping(String message, String replymessage, String senderId) {
    ReplyMapping reply = new ReplyMapping();
    logger.info ("info: message:{}, replymessage:{}, message length:{}, replyMessage Length{}, BLOCK_MESSAGEindex:{}"
            , StringUtils.hasText(message)
            , StringUtils.hasText(replymessage)
            , message.length() > MAX_LENGTH
            , replymessage.length() > MAX_LENGTH
            , BLOCK_KEYWORD.indexOf(message) > 0);
    if (StringUtils.hasText(message)
        || StringUtils.hasText(replymessage)
        || message.length() > MAX_LENGTH
        || replymessage.length() > MAX_LENGTH
        || BLOCK_KEYWORD.indexOf(message) > 0) {
      return "わかんない";
    }
    reply.setMessage(message);
    reply.setReplyMessage(replymessage);
    reply.setCommitUserID(senderId);
    replyRepository.save(reply);
    return "わかった";
  }

  @Override
  public Message queryReplyMessage(String message) {
    List<ReplyMapping> reply = replyRepository.findByMessage(message);
    ReplyMapping replyMessage;
    Message rtnMsg;
    if (reply.size() > 0) {
      replyMessage = reply.get(0);
      if (null == replyMessage.getReplyType()
          || "Text".equalsIgnoreCase(replyMessage.getReplyType())) {
        rtnMsg = new TextMessage(replyMessage.getReplyMessage());
        return rtnMsg;
      }

      if ("Image".equalsIgnoreCase(replyMessage.getReplyType())) {
        try {
          rtnMsg =
              new ImageMessage(
                  new URI(replyMessage.getReplyMessage()), new URI(replyMessage.getReplyMessage()));
        } catch (URISyntaxException e) {
          logger.error(e);
          return new TextMessage("錯誤");
        }

        return rtnMsg;
      }
    }
    return null;
  }

  @Override
  public String deleteReplyMessage(String message) {
    List<ReplyMapping> replyList = replyRepository.findByMessage(message);
    String replyMessage;
    if (replyList.size() > 0) {
      for (ReplyMapping item : replyList) {
        replyRepository.deleteById(item.getId());
      }
      replyMessage = "わかった";
    } else {
      replyMessage = "なに?";
    }
    return replyMessage;
  }

  @Override
  public String listMessage() {
    List<ReplyMapping> replyList = replyRepository.findAll();
    return new Gson().toJson(replyList);
  }

  @Override
  public String saveImageMapping(String message, String replyUrl, String senderId) {
    if (StringUtils.hasText(message)
        || StringUtils.hasText(replyUrl)
        || message.length() > MAX_LENGTH
        || replyUrl.length() > MAX_LENGTH
        || BLOCK_KEYWORD.indexOf(message) > 0) {
      return "わかんない";
    }
    ReplyMapping reply = new ReplyMapping();
    reply.setMessage(message);
    reply.setReplyMessage(replyUrl);
    reply.setReplyType("Image");
    reply.setCommitUserID(senderId);
    replyRepository.save(reply);
    return "わかった";
  }
}
