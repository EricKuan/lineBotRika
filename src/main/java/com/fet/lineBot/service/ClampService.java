package com.fet.lineBot.service;

import com.fet.lineBot.domain.model.FBPostData;

import java.io.IOException;
import java.util.List;

public interface ClampService {

  List<String> queryAnotherSide(int storyNum);

  /**
   * 查詢最新貼文的 FB 連結
   *
   * @return
   */
  FBPostData queryFBNewestPost();

  /**
   * 查詢最新話的 FB 連結
   *
   * @return
   */
  FBPostData queryFBNewestStoryPost();

  /**
   * 查詢輕小說文庫的小說
   *
   * @param novelNum
   * @return
   */
  String getNovel(int novelNum);

  /**
   * 透過伺服器去撈取指定的網址
   * @param url
   * @return
   */
  String getUrl(String url) throws IOException;


  String getVoteResult()  ;
}
