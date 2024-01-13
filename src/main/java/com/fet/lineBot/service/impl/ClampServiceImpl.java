package com.fet.lineBot.service.impl;

import com.fet.lineBot.domain.dao.MangaDataRepository;
import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.domain.model.MangaData;
import com.fet.lineBot.service.ClampService;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ClampServiceImpl implements ClampService {
  private static FBPostData NEWEST_STORY_CACHED_DATA = null;
  private static FBPostData NEWEST_POST_CACHED_DATA = null;

  @Autowired
  MangaDataRepository mangaDataRepository;

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

    List<DomElement> aList = htmlPage.getByXPath("//ul[@id='mh-chapter-list-ol-0']/li/a");
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
    webClient.getOptions().setJavaScriptEnabled(false);
    webClient.getOptions().setCssEnabled(false);
    webClient.getOptions().setRedirectEnabled(true);
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    webClient.getOptions().setTimeout(10000);
    webClient.setJavaScriptTimeout(5000);
    return webClient;
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
    if (null == NEWEST_STORY_CACHED_DATA) {
      return NEWEST_POST_CACHED_DATA;
    } else {
      return NEWEST_STORY_CACHED_DATA;
    }
  }

  //    @Scheduled(initialDelay = 120000, fixedRate = 300000)
  private void getNewestPostBySchedule() throws IOException {
    WebClient webClient = getFBWebClient();
    try {

      HtmlPage page = webClient.getPage("http://www.facebook.com/plugins/likebox.php?href=https%3A%2F%2Fwww.facebook.com%2FWishswing&width=400&height=700&colorscheme=light&show_faces=true&header=true&stream=true&show_border=true");
      /* 切出包含貼文的 DIV */
      DomElement dom = (DomElement) page.getByXPath("//div[@role=\"feed\"]").get(0);

      for (Object ele : dom.getByXPath("./div")) {
        FBPostData data = new FBPostData();
        List<String> hashTag = new ArrayList<>();
        AtomicReference<String> postUrl = new AtomicReference<>();
        AtomicReference<String> messageStr = new AtomicReference<>();
        AtomicReference<String> imgUrl = new AtomicReference<>();
        log.info("=======");
        DomElement bigDiv = (DomElement) ele;
        bigDiv.getByXPath(".//div[@data-testid=\"post_message\"]").stream().forEach(item -> {
          DomElement postMessage = (DomElement) item;
          DomElement message = (DomElement) postMessage.getByXPath(".//p").get(0);
          messageStr.set(message.asNormalizedText());
        });
        bigDiv.getByXPath(".//div[@class=\"mtm\"]/div/a/img").stream().forEach(item -> {
          DomElement img = (DomElement) item;
          imgUrl.set(img.getAttribute("src"));
        });

        if (StringUtils.isBlank(imgUrl.get())) {

          DomElement img = (DomElement) bigDiv.getByXPath(".//div[@class=\"uiScaledImageContainer\"]/img").stream().findFirst().orElse(new DomElement("stage", "stage", null, new HashMap<>()));
          imgUrl.set(img.getAttribute("src"));
        }

        bigDiv.getByXPath(".//a[@target=\"_blank\"]").stream().forEach(item -> {
          DomElement aLink = (DomElement) item;
          if (aLink.getAttribute("href").indexOf("hashtag") > -1) {
            String hashtagUr = aLink.getAttribute("href").split("/")[2].split("\\?")[0];
            hashTag.add(hashtagUr);
          }

          if (aLink.getAttribute("href").startsWith("https")) {
            postUrl.set(aLink.getAttribute("href"));
          }
        });

        log.info("message: {}", messageStr.get());
        log.info("hashTag: {}", new Gson().toJson(hashTag));
        log.info("imgUrl: {}", imgUrl.get());
        log.info("postUrl: {}", postUrl.get());
        /* 處理貼文 ID */

        String[] split = postUrl.get().split("/");
        String storyId = split[split.length - 1];
        log.info("storyId: {}", storyId);
        data.setStoryId(Long.parseLong(storyId));
        data.setImgUrl(imgUrl.get());

        if (hashTag.contains(checkHashTeg)) {
          if (null != NEWEST_STORY_CACHED_DATA) {
            if (data.getStoryId() > NEWEST_STORY_CACHED_DATA.getStoryId()) {
              NEWEST_STORY_CACHED_DATA = data;
            }
          } else {
            NEWEST_STORY_CACHED_DATA = data;
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
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      NEWEST_STORY_CACHED_DATA = NEWEST_POST_CACHED_DATA;
    } finally {
      Optional.ofNullable(webClient).ifPresent(WebClient::close);
    }

    System.gc();
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

  /**
   * 讓 heroku 不會自動休眠
   */
  @Scheduled(initialDelay = 120000, fixedRate = 1200000)
  private void renewHeroku() {
    log.info("heartbeat");
    HttpResponse<String> response = Unirest.get(selfLocation).asString();
    log.debug(response.getBody());
  }

  @Override
  public String getNovel(int novelNum) {
    WebClient webClient = null;
    StringBuilder result = new StringBuilder();
    try {
      webClient = getWebClient();
      String baseUrl = "https://www.wenku8.net/novel/1/" + novelNum + "/";
      webClient.waitForBackgroundJavaScript(100);
      HtmlPage page = webClient.getPage(baseUrl);
      List<DomElement> hrefList = page.getBody().getByXPath("//td[@class=\"ccss\"]/a");

      for (DomElement elem : hrefList) {
        String chapterUrl = baseUrl + elem.getAttribute("href");
        HtmlPage chapterPage = webClient.getPage(chapterUrl);
        webClient.waitForBackgroundJavaScript(500);

        chapterPage.getBody().getByXPath("//div[@id=\"content\"]").stream()
            .findFirst().ifPresent(dom -> {
              DomElement content = (DomElement) dom;
              String original = content.asNormalizedText();
              String translation = ZhConverterUtil.toTraditional(original);
              result.append(translation);
            });


      }
    } catch (Exception e) {
      return e.getMessage();
    } finally {
      Optional.ofNullable(webClient).ifPresent(WebClient::close);
      Optional.ofNullable(result).ifPresent(item -> item = null);
    }
    System.gc();
    return result.toString();
  }

  @Override
  public String getUrl(String url) throws IOException {

    WebClient client = getFBWebClient();
    try {
      client.addCookie("fr=13Vcqjgnr538ePt8O..BgwfId.m3.AAA.0.0.BgwfId.AWUgnSp8pKU; Expires=Wed, 08 Sep 2021 11:06:04 GMT; Max-Age=7775999; Domain=facebook.com; Path=/; Secure; HttpOnly", new URL("http://www.facebook.com"), null);
      client.addCookie("sb=HfLBYMfWt2mFgJspwXy52Sof; Expires=Sat, 10 Jun 2023 11:06:05 GMT; Max-Age=63072000; Domain=facebook.com; Path=/; Secure; HttpOnly", new URL("http://www.facebook.com"), null);
      client.getOptions().setCssEnabled(false);
      client.getOptions().setThrowExceptionOnScriptError(false);
      HtmlPage page = client.getPage(url);

      log.info("page: {}", page.asXml());


      return page.asXml();
    } catch (Exception e) {
      return null;
    } finally {
      Optional.ofNullable(client).ifPresent(WebClient::close);
    }
  }

  @Override
  public String getVoteResult() {
    String url = "https://vote2024.cec.gov.tw/zh-TW/P1/00000000000000000.html";
    String response = "unKnow";
    try {
      WebClient client = getFBWebClient();
      client.addCookie("fr=13Vcqjgnr538ePt8O..BgwfId.m3.AAA.0.0.BgwfId.AWUgnSp8pKU; Expires=Wed, 08 Sep 2021 11:06:04 GMT; Max-Age=7775999; Domain=facebook.com; Path=/; Secure; HttpOnly", new URL("http://www.facebook.com"), null);
      client.addCookie("sb=HfLBYMfWt2mFgJspwXy52Sof; Expires=Sat, 10 Jun 2023 11:06:05 GMT; Max-Age=63072000; Domain=facebook.com; Path=/; Secure; HttpOnly", new URL("http://www.facebook.com"), null);
      client.getOptions().setCssEnabled(false);
      client.getOptions().setThrowExceptionOnScriptError(false);
      HtmlPage page = client.getPage(url);

      List<HtmlDivision> elementList = page.getByXPath("/html/body/div[1]/div[3]/div[3]");
      StringBuilder sb = new StringBuilder();
      String total = elementList.get(0).asNormalizedText();

      List<HtmlTableDataCell> cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[1]/td[4]");

      String koTicket = cellList.get(0).asNormalizedText();
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[1]/td[5]");
      String koPercent = cellList.get(0).asNormalizedText();
      sb.append("柯文哲").append(" 得票數: ").append(koTicket).append(" 得票%: ").append(koPercent).append("\n");
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[2]/td[4]");
      String laiTicket = cellList.get(0).asNormalizedText();
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[2]/td[5]");
      String laiPercent = cellList.get(0).asNormalizedText();
      sb.append("賴清德").append(" 得票數: ").append(laiTicket).append(" 得票%: ").append(laiPercent).append("\n");
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[3]/td[4]");
      String hoTicket = cellList.get(0).asNormalizedText();
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[3]/td[5]");
      String hoPercent = cellList.get(0).asNormalizedText();
      sb.append("侯友宜").append(" 得票數: ").append(hoTicket).append(" 得票%: ").append(hoPercent);


      if (StringUtils.isNotBlank(sb.toString())) {
        response = sb.toString();
      }


    } catch (IOException e) {
      throw new RuntimeException(e);
    }


    return response;
  }

  @Override
  public String getVoteResultForPolitical() {
    String url = "https://vote2024.cec.gov.tw/zh-TW/L4/00000000000000000.html";
    String response = "unKnow";
    try {
      WebClient client = getFBWebClient();
      client.addCookie("fr=13Vcqjgnr538ePt8O..BgwfId.m3.AAA.0.0.BgwfId.AWUgnSp8pKU; Expires=Wed, 08 Sep 2021 11:06:04 GMT; Max-Age=7775999; Domain=facebook.com; Path=/; Secure; HttpOnly", new URL("http://www.facebook.com"), null);
      client.addCookie("sb=HfLBYMfWt2mFgJspwXy52Sof; Expires=Sat, 10 Jun 2023 11:06:05 GMT; Max-Age=63072000; Domain=facebook.com; Path=/; Secure; HttpOnly", new URL("http://www.facebook.com"), null);
      client.getOptions().setCssEnabled(false);
      client.getOptions().setThrowExceptionOnScriptError(false);
      HtmlPage page = client.getPage(url);

      List<HtmlDivision> elementList = page.getByXPath("/html/body/div[1]/div[3]/div");
      StringBuilder sb = new StringBuilder();
      String total = elementList.get(0).asNormalizedText();

      List<HtmlTableDataCell> cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[3]/td[3]");
      String koTicket = cellList.get(0).asNormalizedText();
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[3]/td[4]");
      String koPercent = cellList.get(0).asNormalizedText();
      sb.append("台灣民眾黨").append(" 得票數: ").append(koTicket).append(" 得票%: ").append(koPercent).append("\n");
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[12]/td[3]");
      String laiTicket = cellList.get(0).asNormalizedText();
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[12]/td[4]");
      String laiPercent = cellList.get(0).asNormalizedText();
      sb.append("民主進步黨").append(" 得票數: ").append(laiTicket).append(" 得票%: ").append(laiPercent).append("\n");
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[15]/td[3]");
      String hoTicket = cellList.get(0).asNormalizedText();
      cellList = page.getByXPath("/html/body/div[2]/table/tbody/tr[15]/td[4]");
      String hoPercent = cellList.get(0).asNormalizedText();
      sb.append("中國國民黨").append(" 得票數: ").append(hoTicket).append(" 得票%: ").append(hoPercent).append("\n");
      sb.append(total);
      String trim = total.split(":")[1].trim();
      String[] ticketArray = trim.split("/");
      Integer opened = Integer.valueOf(ticketArray[0]);
      Integer boxed = Integer.valueOf(ticketArray[1]);

      float openedPercent = (float) opened / boxed;
      sb.append("\n已開票所百分比： ").append(openedPercent);

      if (StringUtils.isNotBlank(sb.toString())) {
        response = sb.toString();
      }


    } catch (IOException e) {
      throw new RuntimeException(e);
    }


    return response;
  }
}
