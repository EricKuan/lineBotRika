package com.fet.lineBot.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "BONUS_VOTE_DATA")
@Data
public class BonusPhotoData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "LINE_NAME")
    private String lineName;

    @Column(name = "PIECE_NAME")
    private String pieceName;

    @Column(name = "CHARACTER_NAME")
    private String characterName;

    @Column(name = "CreateDate")
    private Date createDate;
}
