package com.fet.lineBot.service;

import java.util.List;
import com.fet.lineBot.domain.model.FBPostData;

public interface ClampService {

  public String queryVoteResult();

  public List<String> queryAnotherSide(int storyNum);

  /**
   * 查詢最新貼文的 FB 連結
   * 
   * @return
   */
  public FBPostData queryFBNewestPost();

  /**
   * 查詢最新話的 FB 連結
   * 
   * @return
   */
  public FBPostData queryFBNewestStoryPost();
  
  /**
   * 查詢輕小說文庫的小說
   * @param novelNum
   * @return
   */
  public String getNovel(int novelNum);
}
