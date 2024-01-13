package lineBot;

import com.fet.lineBot.Application;
import com.fet.lineBot.domain.dao.BonusPhotoDataRepository;
import com.fet.lineBot.domain.model.BonusPhotoData;
import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.domain.model.YoutubeLiveData;
import com.fet.lineBot.service.BonusPhotoService;
import com.fet.lineBot.service.ClampService;
import com.fet.lineBot.service.YoutubeService;
import com.fet.lineBot.service.impl.ClampServiceImpl;
import com.fet.lineBot.service.impl.TwitterServiceImpl;
import com.fet.lineBot.service.impl.YoutubeServiceImpl;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import com.pkslow.ai.AIClient;
import com.pkslow.ai.GoogleBardClient;
import com.pkslow.ai.domain.Answer;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.Get2UsersIdTweetsResponse;
import com.twitter.clientlib.model.Tweet;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@Log4j2
public class TestCase {

    Gson gson = new Gson();
    @Autowired
    BonusPhotoDataRepository bonusRepo;
    @Autowired
    BonusPhotoService bonusService;
    @Autowired
    ClampService clampService;

    @Autowired
    YoutubeService youtubeService;

    @Value("${rikaService.chatGPTKey}")
    private String chatGPTKey;

    @Test
    public void test02() {
        ClampServiceImpl service = new ClampServiceImpl();
        List<String> urlList = service.queryAnotherSide(1);
        System.out.println(new Gson().toJson(urlList));
    }

    @Test
    public void test03() {
        ClampServiceImpl service = new ClampServiceImpl();
        FBPostData urlList = service.queryFBNewestPost();
        System.out.println(new Gson().toJson(urlList));
    }

    @Test
    public void test04() {
        //	  HttpResponse<String> response = Unirest.post("https://notify-api.line.me/api/notify")
        //	        .header("Authorization", "Bearer " +
        // "Z9GcsPjrnaP8WHaiQnuJNEUS0zMArqFyLSHIiQw5MJI").multiPartContent().field("message", "ttest")
        //	        .asString();
        HttpResponse<String> response =
                Unirest.get(
                        "https://m.facebook.com/page_content_list_view/more/?page_id=542665685764290&start_cursor=1&num_to_fetch=20&surface_type=timeline")
                        .asString();
        System.out.println(response.getBody());
        String body = response.getBody();
        String[] splits = body.split("story_fbid=");
        List<String> postIdList = new ArrayList<String>();
        for (String str : splits) {
            String postId = str.substring(0, str.indexOf("&"));
            if (!postIdList.contains(postId)) {
                postIdList.add(postId);
            }
        }
        System.out.println(new Gson().toJson(postIdList));
    }

    @Test
    public void test05() throws JSONException, IOException {


    }

    @Test
    public void test06() throws Exception {
        WebClient webClient = new WebClient();
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setRedirectEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setTimeout(10000);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        webClient.getOptions().setDoNotTrackEnabled(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        String baseUrl = "https://www.wenku8.net/novel/3/#novelNum#/";
        webClient.waitForBackgroundJavaScript(100);
        Map<String, String> titleMap = new HashMap<String, String>();
        for (int i = 1000; i < 2000; i++) {
            String novelNum = String.valueOf(i);
            String novelUrl = baseUrl.replaceAll("#novelNum#", novelNum);
            try {
                HtmlPage page = webClient.getPage(novelUrl);
                DomElement title =
                        (DomElement) page.getByXPath("//div[@id=\"title\"]").stream().findFirst().get();
                if (StringUtils.isNotBlank(title.asNormalizedText())) {
                    titleMap.put(novelNum, title.asNormalizedText());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(new Gson().toJson(titleMap));
    }

    @Test
    public void test07() throws Exception {
        ClampServiceImpl service = new ClampServiceImpl();
        String novel = service.getNovel(1861);

        FileOutputStream fo = new FileOutputStream(new File("F:\\novel\\從零開始的異世界生活.txt"));
        fo.write(novel.getBytes());
        fo.flush();
        fo.close();
    }

    @Test
    public void test08() throws Exception {

        YoutubeServiceImpl youtubeService = new YoutubeServiceImpl();

        List<YoutubeLiveData> live = youtubeService.searchLiveByChannelId("UCl_gCybOJRIgOXw6Qb4qJzQ");

        log.info(new Gson().toJson(live));
    }

    @Test
    public void test09() throws Exception {
        YoutubeServiceImpl youtubeService = new YoutubeServiceImpl();

        youtubeService.scheduleClamYoutubeData();

    }

    @Test
    public void test10() throws JSONException, IOException {
        HttpResponse<String> responses =
                Unirest.get(
                        "https://www.facebook.com/plugins/likebox.php?href=https%3A%2F%2Fwww.facebook.com%2FWishswing&width=400&height=700&colorscheme=light&show_faces=true&header=true&stream=true&show_border=true")
                        .asString();
        log.info("response: {}", responses.getBody());
    }

    @Test
    public void test11() {
        List<BonusPhotoData> ten = bonusRepo.findByDate(10, 2020);
        List<BonusPhotoData> eleven = bonusRepo.findByDate(11, 2020);


        log.info("ten:{}", gson.toJson(ten));
        log.info("eleven:{}", gson.toJson(eleven));
        List<BonusPhotoData> all = bonusRepo.findAll();

        log.info("all:{}", gson.toJson(all));

    }

    @Test
    public void test12() {
        List<BonusPhotoData> bonusPhotoVoteData = bonusService.findBonusPhotoVoteData(2020, 11);
        log.info("all:{}", gson.toJson(bonusPhotoVoteData));

    }

    @Test
    public void test13() {
        clampService.queryFBNewestPost();
    }

    @Test
    public void test14() {
        try {
            youtubeService.searchLiveByChannelId("UCgL6PS1vba90zrZW9xmiwng");
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Test
    public void test15()throws IOException {
        String bearerToken = "";
        TwitterCredentialsBearer credentials = new TwitterCredentialsBearer(bearerToken);
        TwitterApi apiInstance = new TwitterApi(credentials);

        String id = "";
        try {
            Get2UsersIdTweetsResponse result = apiInstance.tweets().usersIdTweets(id)
                    .maxResults(5)
                    .execute();
            result.getData().get(0).getId();
            result.getData().get(0).getText();
            log.info("result: {}" , new Gson().toJson(result));
        } catch (ApiException e) {
            System.err.println("Exception when calling TweetsApi#usersIdTweets");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }

    @Test
    public void test16() throws JSONException, IOException {

        TwitterServiceImpl twitterService = new TwitterServiceImpl();
        Get2UsersIdTweetsResponse tweetList = twitterService.getTweetList();
        Tweet newestTweet = twitterService.getNewestTweet();

        log.info("tweetList: {}" , new Gson().toJson(tweetList));
        log.info("newestTweet: {}" , new Gson().toJson(newestTweet));

    }

    @Test
    public void test18() throws JSONException, IOException {

        AIClient client = new GoogleBardClient("ZAgUwOXtgtONcYz3p64xnZYBcvgdlqZ0swffPoxHwiQwa53LxeF_2lzLbua-IxWc9NYQOg.;sidts-CjEBSAxbGZDB2CAkHYNe5sJDv-x9jr8ETUmPFEKCQutGgwNdUfehw8JOPCofQHai9o0JEAA");
        Answer answer = client.ask("如何使用 java 呼叫 brad api");

        log.info("answer: {}", answer.getChosenAnswer());
    }

    /** 驗證 twitter API */
    @Test
    public void test19() throws URISyntaxException, IOException {
        String tweetResponse = null;

        String ids = "235549538";
        String bearerToken = "AAAAAAAAAAAAAAAAAAAAAK3vVwEAAAAAR2%2FVh1puEqXhA%2B5LheCIe7PvpEQ%3DyYgDG889lsRN0ZTDeX0JEbToWHDiSZmWaYL8kAsyrgWToA9E7d";

        CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD).build())
            .build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets");



        ArrayList<NameValuePair> queryParameters;
        queryParameters = new ArrayList<>();
        queryParameters.add(new BasicNameValuePair("ids", ids));
        queryParameters.add(new BasicNameValuePair("tweet.fields", "created_at"));
        uriBuilder.addParameters(queryParameters);



        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            tweetResponse = EntityUtils.toString(entity, "UTF-8");
        }
        log.info("tweetResponse: {}", tweetResponse);
    }

    /**
     * 撈取中選會網頁資料
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void test20() throws URISyntaxException, IOException {
        String voteResult = clampService.getVoteResult();
        log.info("voteResult:{}", voteResult);

        String voteResultForPolitical = clampService.getVoteResultForPolitical();
        log.info("voteResultForPolitical:{}", voteResultForPolitical);
    }
}
