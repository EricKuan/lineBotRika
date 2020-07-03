package com.fet.lineBot.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MEMBER")
@Data
public class MemberData {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(name = "USER_ID")
  private String userId;

  @Column(name = "LINE_NAME")
  private String lineName;

  @Column(name = "EXPIRATION_DATE")
  private Date expirationDate;
}
