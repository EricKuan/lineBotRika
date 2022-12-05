package com.fet.lineBot.service;

import com.twitter.clientlib.model.Get2UsersIdTweetsResponse;
import com.twitter.clientlib.model.Tweet;

public interface TwitterService {
    Tweet getNewestTweet();
    Get2UsersIdTweetsResponse getTweetList();
}
