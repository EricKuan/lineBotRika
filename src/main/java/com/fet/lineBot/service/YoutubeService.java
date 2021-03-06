package com.fet.lineBot.service;

import com.fet.lineBot.domain.model.CheckYoutubeLiveNotifyData;
import com.fet.lineBot.domain.model.ClipVideoInfo;
import com.fet.lineBot.domain.model.YoutubeLiveData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface YoutubeService {

  List<YoutubeLiveData> searchUpcomingByChannelId(String channelId)
      throws GeneralSecurityException, IOException;

  List<YoutubeLiveData> searchLiveByChannelId(String channelId)
      throws GeneralSecurityException, IOException;

  List<ClipVideoInfo> getClipVideoIdList() throws GeneralSecurityException, IOException;

  CheckYoutubeLiveNotifyData scheduleClamYoutubeData();
}
