package com.fet.lineBot.domain.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "LINE_BOT_REPLY_MAPPING")
@Data
public class ReplyMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(name = "MESSAGE")
  private String message;

  @Column(name = "REPLY_TYPE")
  private String replyType;

  @Column(name = "REPLY_MESSAGE")
  private String replyMessage;

  @Column(name = "COMMIT_USER_ID")
  private String commitUserID;
}
