package com.fet.lineBot.domain.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

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
