package lineBot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fet.lineBot.domain.model.YoutubeLiveData;
import com.fet.lineBot.service.impl.YoutubeServiceImpl;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.service.impl.ClampServiceImpl;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;


//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class)
//@WebAppConfiguration
public class TestCase {


    @Test
    public void test() {
        ClampServiceImpl service = new ClampServiceImpl();
        String msg = service.queryVoteResult();
        System.out.println(msg);
    }

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
//	        .header("Authorization", "Bearer " + "Z9GcsPjrnaP8WHaiQnuJNEUS0zMArqFyLSHIiQw5MJI").multiPartContent().field("message", "ttest")
//	        .asString();
        HttpResponse<String> response = Unirest.get("https://m.facebook.com/page_content_list_view/more/?page_id=542665685764290&start_cursor=1&num_to_fetch=20&surface_type=timeline")
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

        HttpResponse<String> responses = Unirest.get("https://m.facebook.com/page_content_list_view/more/?page_id=542665685764290&start_cursor=1&num_to_fetch=20&surface_type=timeline").asString();
        String body = responses.getBody();
        body = body.substring(9);

        System.out.println(body);
        JSONObject jsonObj = new JSONObject(body);
        JSONArray array = jsonObj.getJSONArray("actions");
        System.out.println(array.get(0));
        String html = array.getJSONObject(0).getString("html");
        System.out.println(html);
        URL url = new URL("http://www.example.com");
        StringWebResponse response = new StringWebResponse("<html><head><title>Test</title></head><body>" + html + "</body></html>", url);
        WebClient client = new WebClient();
        HtmlPage page = HTMLParser.parseHtml(response, client.getCurrentWindow());
        System.out.println(page.getTitleText());
//      System.out.println(page.asXml());
        List<DomElement> bodyDivList = page.getBody().getByXPath("./div/div/div/div/div");
        List<DomElement> elementList = bodyDivList.stream().filter(item -> {
            DomElement dom = (DomElement) item;
            return dom.getByXPath("./div[@class=\"story_body_container\"]").size() > 0;
        }).collect(Collectors.toList());
        int i = 0;
        FBPostData data = new FBPostData();
        for (DomElement element : elementList) {
            if (element.getByXPath("./div/div/span/p/a/span").stream().filter((item -> {
                DomElement ele = (DomElement) item;
                System.out.println(ele.asXml());
                return "現實與童話的距離".equalsIgnoreCase(ele.getTextContent());
            })).count() > 0) {
                String storyId = null;
                List<DomElement> storyIdList = element.getByXPath("./div/div/a");
                if (storyIdList.size() > 0) {
                    String href = storyIdList.get(0).getAttribute("href");
                    storyId = href.substring(href.indexOf("=") + 1, href.indexOf("&"));
                }

                if (data.getStoryId() < Long.valueOf(storyId)) {

                    String imgUrl = null;
                    List<DomElement> imgList = element.getByXPath("./div/div/div/a/img");
                    if (imgList.size() > 0) {
                        for (DomElement d : imgList) {
                            System.out.println(d.getAttribute("src"));
                        }
                        imgUrl = imgList.get(0).getAttribute("src");
                    }

                    data.setStoryId(Long.valueOf(storyId));
                    data.setImgUrl(imgUrl);

                }
                i++;
            }

        }

        System.out.println(elementList.size());
        System.out.println(i);

        System.out.println(new Gson().toJson(data));
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
            String novelUrl = baseUrl.replaceAll("#novelNum#",novelNum);
            try {
                HtmlPage page = webClient.getPage(novelUrl);
                DomElement title = (DomElement) page.getByXPath("//div[@id=\"title\"]").stream().findFirst().get();
                if (StringUtils.isNotBlank(title.asText())) {
                    titleMap.put(novelNum, title.asText());
                }
            }catch(Exception e){
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

        YoutubeLiveData upcoming = youtubeService.searchUpcomingByChannelId("UCSBG5KBsczK0mqDxGzZGJFg");

        YoutubeLiveData live = youtubeService.searchLiveByChannelId("UCUKD-uaobj9jiqB-VXt71mA");

        System.out.println(new Gson().toJson(upcoming));
        System.out.println(upcoming.getUrl());

        System.out.println(new Gson().toJson(live));
        System.out.println(live.getUrl());


    }

    @Test
    public void test09() throws Exception {
        YoutubeServiceImpl youtubeService = new YoutubeServiceImpl();

        youtubeService.schduleClambYoutubeData();
        Thread.sleep(5000);
        youtubeService.schduleClambYoutubeData();
        Thread.sleep(5000);
        youtubeService.schduleClambYoutubeData();
    }
}
