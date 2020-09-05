package com.fet.lineBot.service.impl;

import com.fet.lineBot.domain.model.YoutubeLiveData;
import com.fet.lineBot.service.YoutubeService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Log4j2
public class YoutubeServiceImpl implements YoutubeService {

    // 設定 API Key
    @Value("${rikaService.youtubeAPIKey}")
    private  String DEVELOPER_KEY;

    @Value("${rikaService.notifyTime}")
    private long notifyTime;

    // 設定要查找的 channel
    @Value("${rikaService.channelIdList}")
    private String CHANNEL_ID_LIST;

    // 直播通知用 token
    @Value("${rikaService.youtubeNotifyToken}")
    private String token;

    private static final String APPLICATION_NAME = "lineBot";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Map<String, YoutubeLiveData> YOUTUBE_CACHE_MAP_U = new HashMap<>();
    private static Map<String, YoutubeLiveData> YOUTUBE_CACHE_MAP_L = new HashMap<>();



    @Scheduled(initialDelay = 120000, fixedRate = 600000)
    public void schduleClambYoutubeData() {
        String[] channelIdList = CHANNEL_ID_LIST.split(",");

        for (String channelId : channelIdList) {
            try {
                //1. 處理 upcoming
                Optional.ofNullable(searchUpcomingByChannelId(channelId)).ifPresent(upcoming ->{
                    Date now = new Date();
                    if(upcoming.getLiveDate().getTime() - now.getTime()<notifyTime
                            && !YOUTUBE_CACHE_MAP_U.containsKey(upcoming.getChannelId())){
                        YOUTUBE_CACHE_MAP_U.put(upcoming.getChannelId(),upcoming);
                        log.debug("upcoming: {}\n title: {}\n url:{}",upcoming.getChannelId(),upcoming.getTitle(),upcoming.getUrl());
                        log.debug("img: {}\n largImg: {}",upcoming.getImgUrl(),upcoming.getLargeImgUrl());
                        // TODO: 發送訊息
                        sendNotify(upcoming);
                    }
                });


                //2. 處理 live
                Optional.ofNullable(searchLiveByChannelId(channelId)).ifPresent(live ->{
                    if(!YOUTUBE_CACHE_MAP_L.containsKey(live.getChannelId())){
                        YOUTUBE_CACHE_MAP_L.put(live.getChannelId(),live);
                        log.debug("live: {}\n title: {}\n url:{}",live.getChannelId(),live.getTitle(),live.getUrl());
                        log.debug("img: {}\n largImg: {}",live.getImgUrl(),live.getLargeImgUrl());
                        // TODO: 發送訊息
                        sendNotify(live);
                    }

                });

            } catch (GeneralSecurityException gsEx) {
                log.error(gsEx);
            } catch (IOException ioEx) {
                log.error(ioEx);
            }
        }


    }

    @Override
    public YoutubeLiveData searchUpcomingByChannelId(String channelId) throws GeneralSecurityException, IOException {
        SearchListResponse searchListResponse = searchAPI(channelId, "upcoming", "video");
        YoutubeLiveData rtnObj = transLiveData(channelId,"upcoming",searchListResponse);
        return rtnObj;
    }

    @Override
    public YoutubeLiveData searchLiveByChannelId(String channelId) throws GeneralSecurityException, IOException {
        SearchListResponse searchListResponse = searchAPI(channelId, "live", "video");
        YoutubeLiveData rtnObj = transLiveData(channelId,"live",searchListResponse);
        return rtnObj;
    }

    /**
     * 將搜尋資料轉換為 YoutuberData 物件
     * @param channelId
     * @param searchListResponse
     * @return
     */
    private YoutubeLiveData transLiveData(String channelId, String broadCastType, SearchListResponse searchListResponse) throws GeneralSecurityException, IOException {
        YoutubeLiveData rtnObj = new YoutubeLiveData();

        log.debug("response: {}", searchListResponse);

        Optional<SearchResult> result = searchListResponse.getItems().stream()
                .filter(item -> Optional.ofNullable(item.getId()).isPresent())
                .filter(item -> Optional.ofNullable(item.getId().getVideoId()).isPresent()).findFirst();

        if(result.isPresent()) {
            rtnObj.setChannelId(channelId);
            rtnObj.setLiveBroadcastContent(broadCastType);
            rtnObj.setVideoId(result.get().getId().getVideoId());
            rtnObj.setTitle(result.get().getSnippet().getTitle());
            rtnObj.setCreateDate(new Date());
            Optional.ofNullable(result.get().getSnippet().getThumbnails()).ifPresent(
                    thumbanail -> {
                        rtnObj.setImgUrl(thumbanail.getMedium().getUrl());
                        rtnObj.setLargeImgUrl(thumbanail.getHigh().getUrl());
                    }
            );

            /* 開始處理 video 日期資訊 */
            VideoListResponse videoListResponse = searchVideoInfoById(rtnObj.getVideoId());
            log.debug("video response: {}",videoListResponse);
            videoListResponse.getItems().stream().filter(item -> Optional.ofNullable(item.getLiveStreamingDetails()).isPresent())
                    .filter(item -> Optional.ofNullable(item.getLiveStreamingDetails().getScheduledStartTime()).isPresent())
                    .findFirst().ifPresent( item -> {
                        log.debug("dateTime:{}", item.getLiveStreamingDetails().getScheduledStartTime());
                        log.debug("Date:{}", new Date(item.getLiveStreamingDetails().getScheduledStartTime().getValue()));
                        rtnObj.setLiveDate(new Date(item.getLiveStreamingDetails().getScheduledStartTime().getValue()));
            });

            return rtnObj;
        }

        return null;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    private static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    /**
     * 查詢頻道特定事件型態的資訊
     * @param channelId
     * @param eventType
     * @param type
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private SearchListResponse searchAPI(String channelId, String eventType, String type) throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        // Define and execute the API request
        YouTube.Search.List request = youtubeService.search()
                .list("snippet");
        return request.setKey(DEVELOPER_KEY)
                .setChannelId(channelId)
                .setEventType(eventType)
                .setType(type)
                .setOrder("date")
                .execute();

    }

    /**
     * 查詢影片資訊
     * @param id
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private VideoListResponse searchVideoInfoById(String id) throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        YouTube.Videos.List request = youtubeService.videos().list("contentDetails,liveStreamingDetails");

        return request.setKey(DEVELOPER_KEY).setId(id).execute();

    }

    private void sendNotify(YoutubeLiveData data) {
        HttpResponse<String> response =
                Unirest.post("https://notify-api.line.me/api/notify")
                        .header("Authorization", "Bearer " + token)
                        .multiPartContent()
                        .field("message", data.getUrl())
                        .field("imageFullsize", data.getLargeImgUrl())
                        .field("imageThumbnail", data.getImgUrl())
                        .asString();
        log.debug(response.getBody());
    }
}
