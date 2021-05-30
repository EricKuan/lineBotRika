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
import com.fet.lineBot.service.impl.YoutubeServiceImpl;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.parser.neko.HtmlUnitNekoHtmlParser;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
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

        HttpResponse<String> responses =
                Unirest.get(
                        "https://m.facebook.com/page_content_list_view/more/?page_id=542665685764290&start_cursor=1&num_to_fetch=50&surface_type=timeline")
                        .asString();
        String body = responses.getBody();
        body = body.substring(9);

        System.out.println(body);
        JSONObject jsonObj = new JSONObject(body);
        JSONArray array = jsonObj.getJSONArray("actions");
        System.out.println(array.get(0));
        String html = array.getJSONObject(0).getString("html");
//        System.out.println(html);
        URL url = new URL("http://www.example.com");
        StringWebResponse response =
                new StringWebResponse(
                        "<html><head><title>Test</title></head><body>" + html + "</body></html>", url);
        WebClient client = new WebClient();
        WebWindow webWindow = client.getCurrentWindow();
        HtmlUnitNekoHtmlParser parser = new HtmlUnitNekoHtmlParser();
        HtmlPage page = parser.parseHtml(response, webWindow);

        System.out.println(page.getTitleText());
        //      System.out.println(page.asXml());
        List<DomElement> bodyDivList = page.getBody().getByXPath("./div/div/div/div/div");
        List<DomElement> elementList =
                bodyDivList.stream()
                        .filter(
                                item -> {
                                    DomElement dom = (DomElement) item;
                                    return dom.getByXPath("./div[@class=\"story_body_container\"]").size() > 0;
                                })
                        .collect(Collectors.toList());

        int i = 0;
        FBPostData data = new FBPostData();
        for (DomElement element : elementList) {
            System.out.printf("\n===\n%s\n===", element.asXml());
            if (element.getByXPath("//a[@href=\"/hashtag/現實與童話的距離?__tn__=%2As-R\"]").stream()
                    .filter(
                            (item -> {
                                DomElement ele = (DomElement) item;
                                System.out.println(ele.asXml());
                                List<Object> eleByXPath = ele.getByXPath("//span");
                                for (Object spanElement : eleByXPath) {
                                    DomElement span = (DomElement) spanElement;
                                    System.out.printf("span: %s", span.asXml());
                                    if ("現實與童話的距離".equalsIgnoreCase(span.getTextContent())) {
                                        return true;
                                    }
                                }
                                return false;
                            }))
                    .count()
                    > 0) {
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
            String novelUrl = baseUrl.replaceAll("#novelNum#", novelNum);
            try {
                HtmlPage page = webClient.getPage(novelUrl);
                DomElement title =
                        (DomElement) page.getByXPath("//div[@id=\"title\"]").stream().findFirst().get();
                if (StringUtils.isNotBlank(title.asText())) {
                    titleMap.put(novelNum, title.asText());
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
                        "https://www.facebook.com/Wishswing")
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
        {
            WebClient client = new WebClient();
            try {
                HttpResponse<String> responses = Unirest.get("https://www.facebook.com/pages_reaction_units/more/?page_id=542665685764290&cursor=%7B%22card_id%22%3A%22videos%22%2C%22has_next_page%22%3Atrue%7D&surface=www_pages_home&unit_count=8&referrer&privacy_mutation_token=eyJ0eXBlIjowLCJjcmVhdGlvbl90aW1lIjoxNjIyMjY2NDEwLCJjYWxsc2l0ZV9pZCI6NjgzMjQxNzc1NzExMjE3fQ%3D%3D&fb_dtsg_ag&__user=0&__a=1&__dyn=7AgNe5Gmawgrolg5K8G6EjheC1szobpEnz8nwgU5GexZ3ocWwAyUuKewhE4mdwJx64e2q3qcw8258e8hwj82oG3i0wpk2u2-263WWwSxu15wgE46fw9C48szU2mwwwg8vy8465o-cypo7y1NwgEcHAy8aEaoG1HwOwnolwBgK7qxS18wIw9i1uG3G1lwlE-7EjxGm1jxe3C0D85a2W5olwUwlonwhE2Lw5dwp8Gdw&__csr=&__req=3&__hs=18776.PHASED%3ADEFAULT.2.0.0.0&dpr=1&__ccg=EXCELLENT&__rev=1003878230&__s=qe4b53%3A6yieno%3Achk9wx&__hsi=6967581178345799253-0&__comet_req=0&__spin_r=1003878230&__spin_b=trunk&__spin_t=1622266410").asString();
//                log.info("response: {}",responses);
                String body = responses.getBody();
                body = body.substring(122,body.indexOf("jsmods")-6);
                body = StringEscapeUtils.unescapeJava(body);
                log.info("response: {}",body);

                String html = body;
                URL url = new URL("http://www.example.com");
                StringWebResponse response =
                        new StringWebResponse(
                                "<html><head><title>Test</title></head><body>" + html + "</body></html>", url);

                WebWindow webWindow = client.getCurrentWindow();
                HtmlUnitNekoHtmlParser parser = new HtmlUnitNekoHtmlParser();
                HtmlPage page = parser.parseHtml(response, webWindow);

                /* 切出包含貼文的 DIV */
                log.info(page.asXml());
                List<DomElement> bodyDivList = page.getBody().getByXPath("./div/div/div/div/div");
                List<DomElement> elementList =
                        bodyDivList.stream()
                                .filter(
                                        item -> {
                                            DomElement dom = item;
                                            return dom.getByXPath("./div[@class=\"story_body_container\"]").size() > 0;
                                        })
                                .collect(Collectors.toList());
                FBPostData data = new FBPostData();
                for (DomElement element : elementList) {
                    /* 處理貼文 ID */
                    String storyId = null;
                    List<DomElement> storyIdList = element.getByXPath("./div/div/a");
                    if (storyIdList.size() > 0) {
                        String href = storyIdList.get(0).getAttribute("href");
                        for (DomElement hyperLink : storyIdList) {
                            String att = hyperLink.getAttribute("href");
                            if (att.indexOf("story_fbid") > 0) {
                                href = att;
                                break;
                            }
                        }
                        storyId = href.substring(href.indexOf("=") + 1, href.indexOf("&"));
                    }

                    /* 觀察到新貼文時建立快取圖片路徑 */
                    if (data.getStoryId() < Long.parseLong(storyId)) {

                        String imgUrl = null;
                        /* 切出圖片路徑 */
                        List<DomElement> imgList = element.getByXPath("./div/div/div/a/img");
                        if (imgList.size() > 0) {
                            imgUrl = imgList.get(0).getAttribute("src");
                        }
                        data.setStoryId(Long.valueOf(storyId));
                        data.setImgUrl(imgUrl);
                    }
                    /* 處理最新漫畫回的快取 */
                    /* 切出包含設定檔中 hashTag 的相關貼文 */

                    if (element.getByXPath("./div/div/div/span/p/a/span").stream()
                            .filter(
                                    (item -> {
                                        DomElement ele = (DomElement) item;
                                        return "現實與童話的距離".equalsIgnoreCase(ele.getTextContent());
                                    }))
                            .count()
                            > 0) {
//                    findStoryFromElement(element, storyId);
                    }
                }
                log.info(new Gson().toJson(data));


            } catch (FailingHttpStatusCodeException | MalformedURLException e) {
                log.error(e);
            } finally {
                client.close();
            }
            System.gc();
        }
    }

    @Test
    public void test16() throws JSONException, IOException {

        HttpResponse<String> responses =
                Unirest.get(
                        "https://www.facebook.com/pg/Wishswing/posts/")
                        .asString();

        String body = responses.getBody();
        FileWriter myWriter = new FileWriter("d:\\temp\\20210529\\filename.txt");
        myWriter.write(body);
        log.info("body: \n{}", body);
        try(WebClient client = new WebClient()) {
            URL url = new URL("http://www.example.com");
            WebWindow webWindow = client.getCurrentWindow();
            HtmlUnitNekoHtmlParser parser = new HtmlUnitNekoHtmlParser();
            StringWebResponse response = new StringWebResponse(body, url);
            HtmlPage page = parser.parseHtml(response, webWindow);

            List<DomElement> bodyDivList = page.getBody().getByXPath("//div/div[@data-testid=\"post_message\"]");
            log.info(bodyDivList.size());
            for(DomElement dom:bodyDivList){

                //查詢內文
                DomElement p = (DomElement) dom.getByXPath(".//p").get(0);
                log.info("context: {}", p.getTextContent());

                //查詢網址編號
                try {
                    DomElement link = (DomElement) dom.getByXPath(".//a[@class=\"see_more_link\"]").get(0);
                    log.info("more link: {}", link.getAttribute("href"));
                }catch (Exception e){
//                    log.error(e);
                    log.info("查詢不到 post ID");
                }
                //抓取圖片
                try {
//                    DomElement photo = (DomElement) dom.getByXPath("../*div[@class=\"uiScaledImageContainer\"]").get(0);
//                    log.info("mtm: {}", photo.asXml());
//                    log.info("photo: {}", photo.getAttribute("src"));
                }catch (Exception e){
//                    log.error(e);
                    log.info("查詢不到 photo ID");
                }
                //確認TAG
                int tagCheck = p.getByXPath(".//span").stream().filter(item -> {
                    DomElement domObj = (DomElement) item;
                    if ("現實與童話的距離".equals(((DomElement) item).getTextContent())) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList()).size();

                if(tagCheck>0){
                    log.info("HITTTTTT");
                    log.info("\n\ndom: {}\n\n", dom.asXml());
                }



            }
        }

    }
}
