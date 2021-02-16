package com.fet.lineBot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Timer;

@Data
@AllArgsConstructor
public class CheckYoutubeLiveNotifyData {
    private Map<String, YoutubeLiveData> YOUTUBE_CACHE_MAP_U;
    private Map<String, Timer> TIMER_CACHE_MAP;
    private Date sysdate;
}
