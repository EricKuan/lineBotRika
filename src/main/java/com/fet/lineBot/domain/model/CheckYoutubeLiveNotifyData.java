package com.fet.lineBot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckYoutubeLiveNotifyData {
    private List<YoutubeLiveData> YOUTUBE_CACHE_MAP_U;
    private Date sysdate;
}
