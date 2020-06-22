package com.fet.lineBot.domain.model;

import lombok.Data;


@Data
public class FBPostData {

  private String imgUrl;
  private Long storyId = (long) 0;
  private boolean comicFlag = false; 
}
