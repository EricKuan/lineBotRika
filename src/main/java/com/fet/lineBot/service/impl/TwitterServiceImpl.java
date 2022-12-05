package com.fet.lineBot.service.impl;

import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.service.TwitterService;
import com.google.gson.Gson;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.Get2UsersIdTweetsResponse;
import com.twitter.clientlib.model.Tweet;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Log4j2
public class TwitterServiceImpl implements TwitterService {


    @Value("${twitter.bearerToken}")
    String bearerToken ;
    @Value("${twitter.maxResult}")
    int maxResult ;
    @Value("${twitter.twitterId}")
    String twitterId ;

    @Value("${rikaService.lineToken}")
    private String token;

    private static Tweet NEWEST_POST_CACHED_DATA = null;


    @Override
    public Tweet getNewestTweet() {
        if(!Optional.ofNullable(NEWEST_POST_CACHED_DATA).isPresent()) {
            Get2UsersIdTweetsResponse tweetList = getTweetList();
            AtomicReference<Tweet> newestTweet = new AtomicReference<Tweet>();
            Optional.ofNullable(tweetList).ifPresent(
                    item -> {
                        newestTweet.set(item.getData().stream().filter(tweet -> {
                            return !StringUtils.startsWithIgnoreCase(tweet.getText(), "RT" );
                        }).max(Comparator.comparing(Tweet::getId)).orElse(new Tweet()));
                    }

            );
            NEWEST_POST_CACHED_DATA = newestTweet.get();
        }

        return NEWEST_POST_CACHED_DATA;
    }

    @Override
    public Get2UsersIdTweetsResponse getTweetList() {
        TwitterCredentialsBearer credentials = new TwitterCredentialsBearer(bearerToken);
        TwitterApi apiInstance = new TwitterApi(credentials);
        Get2UsersIdTweetsResponse result = null;
        try {
            result = apiInstance.tweets().usersIdTweets(twitterId)
                    .maxResults(maxResult)
                    .execute();
            log.info("result: {}", new Gson().toJson(result));
        } catch (ApiException e) {
            log.error("Exception when calling TweetsApi#usersIdTweets");
            log.error("Status code: " + e.getCode());
            log.error("Reason: " + e.getResponseBody());
            log.error("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }

        return result;
    }

    // 自動查詢新推文
    @Scheduled(initialDelay = 120000, fixedRate = 600000)
    private void scheduledTweet(){
        Tweet newestTweet = getNewestTweet();
        if(!Optional.ofNullable(NEWEST_POST_CACHED_DATA).isPresent()){
            NEWEST_POST_CACHED_DATA = newestTweet;
            return;
        }

        Integer oldTweetId = Integer.valueOf(NEWEST_POST_CACHED_DATA.getId());
        Integer newTweetId = Integer.valueOf(newestTweet.getId());

        if(newTweetId>oldTweetId){
            NEWEST_POST_CACHED_DATA = newestTweet;
            sendNotify(newestTweet);
        }


    }
    private void sendNotify(Tweet newestTweet) {
        HttpResponse<String> response =
                Unirest.post("https://notify-api.line.me/api/notify")
                        .header("Authorization", "Bearer " + token)
                        .multiPartContent()
                        .field("message", newestTweet.getText())
                        .asString();
        log.debug(response.getBody());
    }

}
