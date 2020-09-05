package com.fet.lineBot.service;

import com.fet.lineBot.domain.model.YoutubeLiveData;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface YoutubeService {


  YoutubeLiveData searchUpcomingByChannelId(String channelId) throws GeneralSecurityException, IOException ;

  YoutubeLiveData searchLiveByChannelId(String channelId) throws GeneralSecurityException, IOException;


}
