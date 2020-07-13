package com.fet.lineBot.domain.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "MANGA_DATA")
@Data
public class MangaData {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(name = "MANGA_ID")
  private int mangaId;

  @Column(name = "CHAPTER_NO")
  private int chapterNo;

  @Column(name = "PICTURE_NO")
  private int pictureNo;

  @Column(name = "URL")
  private String url;
}
