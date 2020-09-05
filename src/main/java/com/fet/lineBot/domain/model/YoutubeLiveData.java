package com.fet.lineBot.domain.model;

import java.util.Date;

public class YoutubeLiveData {

    private String title;
    private String channelId;
    private String videoId;
    private String liveBroadcastContent;
    private String imgUrl;
    private String largeImgUrl;
    private boolean isNotify;
    private Date liveDate;
    private Date createDate;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getLargeImgUrl() {
        return largeImgUrl;
    }

    public void setLargeImgUrl(String largeImgUrl) {
        this.largeImgUrl = largeImgUrl;
    }

    public Date getLiveDate() {
        return liveDate;
    }

    public void setLiveDate(Date liveDate) {
        this.liveDate = liveDate;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getLiveBroadcastContent() {
        return liveBroadcastContent;
    }

    public void setLiveBroadcastContent(String liveBroadcastContent) {
        this.liveBroadcastContent = liveBroadcastContent;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isNotify() {
        return isNotify;
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
    }

    public String getUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.youtube.com/watch?v=");
        sb.append(this.videoId);
        return sb.toString();
    }
}
