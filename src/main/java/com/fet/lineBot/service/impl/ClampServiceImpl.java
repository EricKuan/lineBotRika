package com.fet.lineBot.service.impl;

import com.fet.lineBot.domain.dao.MangaDataRepository;
import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.domain.model.MangaData;
import com.fet.lineBot.service.ClampService;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.parser.neko.HtmlUnitNekoHtmlParser;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ClampServiceImpl implements ClampService {
  private static FBPostData NEWEST_STORY_CACHED_DATA = null;
  private static FBPostData NEWEST_POST_CACHED_DATA = null;

  @Autowired MangaDataRepository mangaDataRepository;

  @Value("${rikaService.waitForjsTime}")
  private long JS_TIME;

  @Value("${rikaService.lineToken}")
  private String token;

  @Value("${rikaService.selfLocation}")
  private String selfLocation;

  @Value("${rikaService.checkPage}")
  private String checkPage;

  @Value("${rikaService.checkHashTeg}")
  private String checkHashTeg;

  @Value("${rikaService.facebookCookie}")
  private String facebookCookie;


  public List<String> queryAnotherSide(int storyNum) {
    String baseUrl = "https://manmankan.cc/manhua/41287/";
    List<String> pictureUrl = null;
    try {
      List<MangaData> mangaList = mangaDataRepository.findByMangaIdAndChapterNo(1, storyNum);
      if (mangaList.size() < 1) {
        List<String> chapterList = getTopics(baseUrl);
        pictureUrl = getPicturs(chapterList.get(storyNum));
        mangaList = new ArrayList<MangaData>();
        for (int i = 0; i < pictureUrl.size(); i++) {
          MangaData mangaData = new MangaData();
          mangaData.setChapterNo(storyNum);
          mangaData.setPictureNo(i);
          mangaData.setMangaId(1);
          mangaData.setUrl(pictureUrl.get(i));
          mangaDataRepository.save(mangaData);
        }
        return pictureUrl;
      }

      pictureUrl = mangaList.stream().map(MangaData::getUrl).collect(Collectors.toList());

    } catch (Exception e) {
      e.printStackTrace();
    }

    return pictureUrl;
  }

  private List<String> getTopics(String baseUrl) throws IOException {
    WebClient webClient = getWebClient();
    HtmlPage htmlPage = webClient.getPage(baseUrl);
    webClient.waitForBackgroundJavaScript(JS_TIME);

    //		log.info(htmlPage.getElementById("mh-chapter-list-ol-0").asXml());
    List<DomElement> aList = htmlPage.getByXPath("//ul[@id='mh-chapter-list-ol-0']/li/a");
    //		log.info(aList.get(0).getAttribute("href"));
    List<String> urlList =
        aList.stream()
            .map(element -> "https://manmankan.cc" + element.getAttribute("href"))
            .collect(Collectors.toList());

    webClient.close();
    return urlList;
  }

  private List<String> getPicturs(String url) throws IOException {
    WebClient webClient = getJSWebClient();
    List<String> imageUrl = new ArrayList<String>();
    for (int i = 1; i < 30; i++) {
      HtmlPage htmlPage = webClient.getPage(url + "#@page=" + i);
      webClient.waitForBackgroundJavaScript(JS_TIME);
      //			log.info("IMGUel" +  htmlPage.getElementByName("page_1").getAttribute("src"));
      String imgUrl = htmlPage.getElementByName("page_1").getAttribute("src");
      if (imgUrl.contains("undefined")) {
        break;
      }
      imageUrl.add(imgUrl);
    }
    webClient.close();
    return imageUrl;
  }

  private WebClient getWebClient() {
    WebClient webClient = new WebClient();
    webClient.getOptions().setUseInsecureSSL(true);
    webClient.getOptions().setJavaScriptEnabled(false);
    return getWebClient(webClient);
  }

  private WebClient getWebClient(WebClient webClient) {
    webClient.getOptions().setCssEnabled(false);
    webClient.getOptions().setRedirectEnabled(false);
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    webClient.getOptions().setTimeout(10000);
    webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    webClient.getOptions().setDoNotTrackEnabled(true);
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    return webClient;
  }

  private WebClient getJSWebClient() {
    WebClient webClient = new WebClient();
    webClient.getOptions().setUseInsecureSSL(true);
    webClient.getOptions().setJavaScriptEnabled(true);
    return getWebClient(webClient);
  }

  private WebClient getFBWebClient() {
    WebClient webClient = new WebClient();
    webClient.getOptions().setUseInsecureSSL(true);
    webClient.getOptions().setJavaScriptEnabled(true);
    webClient.getOptions().setRedirectEnabled(true);
    webClient.getOptions().setTimeout(5000);
    CookieManager cookiesManager = new CookieManager();
    JSONObject jObject = new JSONObject(facebookCookie);
    JSONObject facebookCookies = jObject.getJSONObject(".facebook.com").getJSONObject(".facebook.com");
    for(String key:facebookCookies.keySet()){
      JSONObject cookiesJSONObject = facebookCookies.getJSONObject(key);

      Cookie cookie = new Cookie(cookiesJSONObject.getString("domain"),
              cookiesJSONObject.getString("name"),
              cookiesJSONObject.getString("value"));
      cookiesManager.addCookie(cookie);
    }
    webClient.setCookieManager(cookiesManager);
    return getWebClient(webClient);
  }

  @Override
  public FBPostData queryFBNewestPost() {
    if (null == NEWEST_POST_CACHED_DATA) {
      try {
        getNewestPostBySchedule();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return NEWEST_POST_CACHED_DATA;
  }

  @Override
  public FBPostData queryFBNewestStoryPost() {
    if (null == NEWEST_STORY_CACHED_DATA) {
      try {
        getNewestPostBySchedule();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return NEWEST_STORY_CACHED_DATA;
  }

//  @Scheduled(initialDelay = 120000, fixedRate = 1200000)
  private void getNewestPostBySchedule() throws IOException {
    WebClient webClient = getFBWebClient();
    try {
      HttpResponse<String> responses = Unirest.get(checkPage).asString();
      log.info("return response: {}", responses.getBody());
      String body = responses.getBody();
      body = body.substring(9);
      log.info("return json: {}", body);
      JSONObject jsonObj = new JSONObject(body);
      JSONArray array = jsonObj.getJSONArray("actions");
      String html = array.getJSONObject(0).getString("html");
      URL url = new URL("http://www.example.com");
      StringWebResponse response =
          new StringWebResponse(
              "<html><head><title>Test</title></head><body>" + html + "</body></html>", url);
      WebClient client = new WebClient();
      WebWindow webWindow = webClient.getCurrentWindow();
      HtmlUnitNekoHtmlParser parser = new HtmlUnitNekoHtmlParser();
      HtmlPage page = parser.parseHtml(response, webWindow);

      /* 切出包含貼文的 DIV */
      log.debug(page.asXml());
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

          findImgFromElement(element, storyId, data);
        }
        /* 處理最新漫畫回的快取 */
        /* 切出包含設定檔中 hashTag 的相關貼文 */

        if (element.getByXPath("./div/div/div/span/p/a/span").stream()
                .filter(
                    (item -> {
                      DomElement ele = (DomElement) item;
                      return checkHashTeg.equalsIgnoreCase(ele.getTextContent());
                    }))
                .count()
            > 0) {
          findStoryFromElement(element, storyId);
        }
      }
      log.debug(new Gson().toJson(data));
      /* 處理最新貼文的快取 */
      if (null != NEWEST_POST_CACHED_DATA) {
        /* 更換暫存資料並發送 Line 通知 */
        if (data.getStoryId() > NEWEST_POST_CACHED_DATA.getStoryId()) {
          NEWEST_POST_CACHED_DATA = data;
          sendNotify(NEWEST_POST_CACHED_DATA);
        }
      } else {
        NEWEST_POST_CACHED_DATA = data;
      }

    } catch (FailingHttpStatusCodeException e) {
      log.error(e);
    } finally {
      webClient.close();
    }
    System.gc();
  }

  /**
   * 找出 img 網址
   *
   * @param element
   * @param storyId
   */
  private void findStoryFromElement(DomElement element, String storyId) {
    FBPostData storyData = new FBPostData();
    findImgFromElement(element, storyId, storyData);
    log.debug(new Gson().toJson(storyData));
    storyData.setComicFlag(true);
    if (null != NEWEST_STORY_CACHED_DATA) {
      if (storyData.getStoryId() > NEWEST_STORY_CACHED_DATA.getStoryId()) {
        NEWEST_STORY_CACHED_DATA = storyData;
      }
    } else {
      NEWEST_STORY_CACHED_DATA = storyData;
    }
  }

  private void findImgFromElement(DomElement element, String storyId, FBPostData storyData) {
    String imgUrl = null;
    List<DomElement> imgList = element.getByXPath("./div/div/div/a/img");
    if (imgList.size() > 0) {
      imgUrl = imgList.get(0).getAttribute("src");
    }
    storyData.setStoryId(Long.valueOf(storyId));
    storyData.setImgUrl(imgUrl);
  }

  private void sendNotify(FBPostData data) {
    HttpResponse<String> response =
        Unirest.post("https://notify-api.line.me/api/notify")
            .header("Authorization", "Bearer " + token)
            .multiPartContent()
            .field("message", "https://www.facebook.com/Wishswing/posts/" + data.getStoryId())
            .field("imageFullsize", data.getImgUrl())
            .field("imageThumbnail", data.getImgUrl())
            .asString();
    log.debug(response.getBody());
  }

  /** 讓 heroku 不會自動休眠 */
  @Scheduled(initialDelay = 120000, fixedRate = 1200000)
  private void renewHeroku() {
    log.info("heartbeat");
    HttpResponse<String> response = Unirest.get(selfLocation).asString();
    log.debug(response.getBody());
  }

  @Override
  public String getNovel(int novelNum) {
    WebClient webClient = null;
    StringBuffer result = new StringBuffer();
    try {
      webClient = getWebClient();
      String baseUrl = "https://www.wenku8.net/novel/1/" + novelNum + "/";
      webClient.waitForBackgroundJavaScript(100);
      HtmlPage page = webClient.getPage(baseUrl);
      //			log.info(page.asXml());
      List<DomElement> hrefList = page.getBody().getByXPath("//td[@class=\"ccss\"]/a");

      for (DomElement elem : hrefList) {
        String chapterUrl = baseUrl + elem.getAttribute("href");
        // log.info(chapterUrl);
        HtmlPage chapterPage = webClient.getPage(chapterUrl);
        webClient.waitForBackgroundJavaScript(500);
        // log.info(chapterPage.asXml());

        chapterPage.getBody().getByXPath("//div[@id=\"content\"]").stream()
                .findFirst().ifPresent( dom -> {
                  DomElement content = (DomElement) dom;
          String original = content.asText();
          String translation = ZhConverterUtil.convertToTraditional(original);
          result.append(translation);
        } );


      }
    } catch (Exception e) {
      return e.getMessage();
    } finally {
      Optional.ofNullable(webClient).ifPresent(WebClient::close);
    }
    System.gc();
    return result.toString();
  }

  @Override
  public String getUrl(String url) {
    log.info("URL: {}", url);
    WebClient fbWebClient = getFBWebClient();
    try {
      HtmlPage page = fbWebClient.getPage(new URL(url));
      return page.asXml();
    }catch (java.io.IOException e){
      return e.getMessage();
    }
  }
}
