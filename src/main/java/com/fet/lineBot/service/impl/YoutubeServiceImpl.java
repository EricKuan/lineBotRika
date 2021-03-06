package com.fet.lineBot.service.impl;

import com.fet.lineBot.domain.model.CheckYoutubeLiveNotifyData;
import com.fet.lineBot.domain.model.ClipVideoInfo;
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
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class YoutubeServiceImpl implements YoutubeService {

    // 設定 API Key
    @Value("${rikaService.youtubeAPIKey}")
    private String DEVELOPER_KEY;

    @Value("${rikaService.notifyTime}")
    private long notifyTime;

    // 設定要查找的 channel
    @Value("${rikaService.channelIdList}")
    private String CHANNEL_ID_LIST;

    // 直播通知用 token
    @Value("${rikaService.youtubeNotifyToken}")
    private String token;

    // 直播通知用 token
    @Value("${rikaService.titleKeyword}")
    private String titleKeyword;

    // 直播通知用 token
    @Value("${rikaService.clipKeyword}")
    private String clipKeyword;

    private static final String APPLICATION_NAME = "lineBot";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Map<String, List<YoutubeLiveData>> YOUTUBE_CACHE_MAP_U = new HashMap<>();
    private static Map<String, Timer> TIMER_CACHE_MAP = new HashMap<>();
    private static List<ClipVideoInfo> CLIP_VIDEO_ID_LIST = new ArrayList<>();

    @Scheduled(cron = "0 */20 * * * *", zone = "Asia/Taipei")
    private void scheduleCheckYoutubeLive() {
        CheckYoutubeLiveNotifyData checkYoutubeLiveNotifyData = scheduleClamYoutubeData();
        String logJsonLiveData = new Gson().toJson(checkYoutubeLiveNotifyData);
        log.info("live data: {}", logJsonLiveData);
    }

    @Override
    public CheckYoutubeLiveNotifyData scheduleClamYoutubeData() {
        log.info("scheduled Start at {}", new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()));
        String[] channelIdList = CHANNEL_ID_LIST.split(",");

        for (String channelId : channelIdList) {
            try {

                // 1. 處理 live
                log.info("channelId: {}", channelId);
                log.info("mapCheck: {}", YOUTUBE_CACHE_MAP_U.containsKey(channelId));
                if (YOUTUBE_CACHE_MAP_U.containsKey(channelId)) {
                    List<YoutubeLiveData> channelDataList = YOUTUBE_CACHE_MAP_U.get(channelId);
                    log.info("data: {}", new Gson().toJson(channelDataList));
                    channelDataList.sort(Comparator.comparing(YoutubeLiveData::getLiveDate));
                    YoutubeLiveData channelData = channelDataList.get(0);
                    Calendar nowDate = Calendar.getInstance();
                    nowDate.add(Calendar.HOUR_OF_DAY, -1);
                    log.info("createTime: {}", channelData.getCreateDate());
                    log.info("nowDate: {}", nowDate.getTime());
                    log.info("checkDate: {}", channelData.getCreateDate().after(nowDate.getTime()));
                    if (channelData.getCreateDate().after(nowDate.getTime())) {
                        log.info("== continue ==");
                        continue;
                    }
                    YOUTUBE_CACHE_MAP_U.remove(channelId);
                }

                List<YoutubeLiveData> upcomingList = searchUpcomingByChannelId(channelId);
                YOUTUBE_CACHE_MAP_U.put(channelId, upcomingList);
                upcomingList.stream().forEach(upcoming -> {
                    if (upcoming.getLiveDate() != null) {
                        String videoId = upcoming.getVideoId();
                        Date now = new Date();
                        log.info("liveDate: {}", upcoming.getLiveDate().getTime());
                        log.info("now: {}", now.getTime());
                        long liveTimeCompare = upcoming.getLiveDate().getTime() - now.getTime();
                        log.info("compare Time: {}", liveTimeCompare);

                        log.info(
                                "upcoming: {}\n title: {}\n url:{}",
                                upcoming.getChannelId(),
                                upcoming.getTitle(),
                                upcoming.getUrl());
                        log.info("img: {}\n largeImg: {}", upcoming.getImgUrl(), upcoming.getLargeImgUrl());

                        /* LIVE 提醒 */
                        log.info("live notify Timer: {}", liveTimeCompare);
                        final StringBuilder notify1 = new StringBuilder().append(videoId).append("_L");
                        if (!TIMER_CACHE_MAP.containsKey(notify1.toString())) {
                            buildNotifyEvent(upcoming, liveTimeCompare, notify1);
                        }
                        /* 提前提醒 */
                        final StringBuilder notify2 = new StringBuilder().append(videoId).append("_N");
                        if (!TIMER_CACHE_MAP.containsKey(notify2.toString())) {
                            long notifySchedule = liveTimeCompare - notifyTime;
                            log.info("notify Schedule Timer: {}", notifySchedule);
                            buildNotifyEvent(upcoming, notifySchedule, notify2);
                        }
                    }
                });


            } catch (GeneralSecurityException gsEx) {
                log.error(gsEx);
            } catch (IOException ioEx) {
                log.error(ioEx);
            }
        }
        CheckYoutubeLiveNotifyData rtnData = createNotifyCheckData();
        return rtnData;
    }

    private CheckYoutubeLiveNotifyData createNotifyCheckData() {
        CheckYoutubeLiveNotifyData rtnData = new CheckYoutubeLiveNotifyData();
        List<YoutubeLiveData> youtubeCache = new ArrayList<>();
        YOUTUBE_CACHE_MAP_U.values().forEach(youtubeCache::addAll);
        rtnData.setYOUTUBE_CACHE_MAP_U(youtubeCache);
        rtnData.setSysdate(new Date());
        return rtnData;
    }

    private void buildNotifyEvent(YoutubeLiveData upcoming, long liveTimeCompare, StringBuilder notify) {
        if (liveTimeCompare < 0) {
            return;
        }
        Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        sendNotify(upcoming);
                        timer.cancel();
                        TIMER_CACHE_MAP.remove(notify.toString());
                    }
                },
                liveTimeCompare);
        TIMER_CACHE_MAP.put(notify.toString(), timer);
    }

    @Override
    public List<YoutubeLiveData> searchUpcomingByChannelId(String channelId)
            throws GeneralSecurityException, IOException {
        if (YOUTUBE_CACHE_MAP_U.containsKey(channelId)) {
            return new ArrayList<>();
        }
        SearchListResponse searchListResponse = searchAPI(channelId, "upcoming", "video");
        return transLiveData(channelId, "upcoming", searchListResponse);
    }

    @Override
    public List<YoutubeLiveData> searchLiveByChannelId(String channelId)
            throws GeneralSecurityException, IOException {
        SearchListResponse searchListResponse = searchAPI(channelId, "live", "video");
        return transLiveData(channelId, "upcoming", searchListResponse);
    }

    @Override
    public List<ClipVideoInfo> getClipVideoIdList() throws GeneralSecurityException, IOException {
        if (CLIP_VIDEO_ID_LIST.size() < 1) {
            searchByKeyword(clipKeyword);
        }

        return CLIP_VIDEO_ID_LIST;
    }

    /**
     * 將搜尋資料轉換為 YoutuberData 物件
     *
     * @param channelId
     * @param searchListResponse
     * @return
     */
    private List<YoutubeLiveData> transLiveData(
            String channelId, String broadCastType, SearchListResponse searchListResponse) {
        List<YoutubeLiveData> rtnList = new ArrayList<>();

        log.info("response: {}", searchListResponse);

        List<SearchResult> youtubeVideoDataList = searchListResponse.getItems().stream()
                .filter(item -> Optional.ofNullable(item.getId()).isPresent())
                .filter(item -> Optional.ofNullable(item.getId().getVideoId()).isPresent()).collect(Collectors.toList());
        if (youtubeVideoDataList.isEmpty()) {
            return new ArrayList<>();
        }
        youtubeVideoDataList.forEach(item -> {
            String title = item.getSnippet().getTitle();
            log.info("titleKeyword:{}, title:{}", titleKeyword, title);
            log.info("checkResult: {}", StringUtils.containsIgnoreCase(title, titleKeyword));
            if (StringUtils.contains(title, titleKeyword)) {
                YoutubeLiveData data = new YoutubeLiveData();
                data.setChannelId(channelId);
                data.setLiveBroadcastContent(broadCastType);
                data.setVideoId(item.getId().getVideoId());
                data.setTitle(item.getSnippet().getTitle());
                data.setCreateDate(new Date());
                Optional.ofNullable(item.getSnippet().getThumbnails())
                        .ifPresent(
                                thumbnail -> {
                                    data.setImgUrl(thumbnail.getMedium().getUrl());
                                    data.setLargeImgUrl(thumbnail.getHigh().getUrl());
                                });

                /* 開始處理 video 日期資訊 */
                try {
                    VideoListResponse videoListResponse = searchVideoInfoById(data.getVideoId());
                    log.debug("video response: {}", videoListResponse);
                    videoListResponse.getItems().stream()
                            .filter(videoInfo -> Optional.ofNullable(videoInfo.getLiveStreamingDetails()).isPresent())
                            .filter(
                                    videoInfo ->
                                            Optional.ofNullable(videoInfo.getLiveStreamingDetails().getScheduledStartTime())
                                                    .isPresent())
                            .findFirst()
                            .ifPresent(
                                    videoInfo -> {
                                        log.debug("dateTime:{}", videoInfo.getLiveStreamingDetails().getScheduledStartTime());
                                        log.debug(
                                                "Date:{}",
                                                new Date(videoInfo.getLiveStreamingDetails().getScheduledStartTime().getValue()));
                                        data.setLiveDate(
                                                new Date(videoInfo.getLiveStreamingDetails().getScheduledStartTime().getValue()));
                                    });
                    rtnList.add(data);
                } catch (IOException | GeneralSecurityException e) {
                    log.error(e);
                }
            }
        });


        return rtnList;
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
     *
     * @param channelId
     * @param eventType
     * @param type
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private SearchListResponse searchAPI(String channelId, String eventType, String type)
            throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        // Define and execute the API request
        YouTube.Search.List request = youtubeService.search().list("snippet");
        return request
                .setKey(DEVELOPER_KEY)
                .setChannelId(channelId)
//        .setEventType(eventType)
                .setMaxResults(Long.valueOf(3))
                .setFields("items(id/videoId,snippet/title,snippet/thumbnails/medium/url,snippet/thumbnails/high/url)")
                .setType(type)
                .setOrder("date")
                .execute();
    }

    /**
     * 查詢影片資訊
     *
     * @param id
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private VideoListResponse searchVideoInfoById(String id)
            throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        YouTube.Videos.List request =
                youtubeService.videos().list("liveStreamingDetails")
                        .setFields("items(liveStreamingDetails/scheduledStartTime)");

        return request.setKey(DEVELOPER_KEY).setId(id).execute();
    }

    private void sendNotify(YoutubeLiveData data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
        StringBuilder sb = new StringBuilder();
        sb.append("\n")
                .append(data.getTitle())
                .append("\n將於 ")
                .append(sdf.format(data.getLiveDate()))
                .append(" 開始\n")
                .append("直播網址：")
                .append(data.getUrl());

        HttpResponse<String> response =
                Unirest.post("https://notify-api.line.me/api/notify")
                        .header("Authorization", "Bearer " + token)
                        .multiPartContent()
                        .field("message", sb.toString())
                        .field("imageFullsize", data.getLargeImgUrl())
                        .field("imageThumbnail", data.getImgUrl())
                        .asString();
        log.debug(response.getBody());
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Taipei")
    private void cleanClipVideoIdCache() {
        CLIP_VIDEO_ID_LIST = new ArrayList<>();
    }

    private synchronized void searchByKeyword(String keyWord)
            throws GeneralSecurityException, IOException {
        if (CLIP_VIDEO_ID_LIST.size() > 0) {
            return;
        }
        log.info("=====start youtube search=====");
        YouTube youtubeService = getService();
        // Define and execute the API request
        YouTube.Search.List request = youtubeService.search().list("snippet");
        SearchListResponse searchResult = request
                .setKey(DEVELOPER_KEY)
                .setType("video")
                .setVideoEmbeddable("true")
                .setQ(keyWord)
                .setMaxResults(Long.valueOf(20))
                .setFields("items(id/videoId,snippet/title)")
                .setOrder("date")
                .execute();
        searchResult.getItems().stream().forEach(item -> {
            ClipVideoInfo clip = new ClipVideoInfo();
            clip.setTitle(item.getSnippet().getTitle());
            clip.setVideoUrl("https://www.youtube.com/embed/" + item.getId().getVideoId());
            CLIP_VIDEO_ID_LIST.add(clip);
        });
        log.info("youtube search result: {}", new Gson().toJson(CLIP_VIDEO_ID_LIST));
        log.info("=====end youtube search=====");
    }
}
