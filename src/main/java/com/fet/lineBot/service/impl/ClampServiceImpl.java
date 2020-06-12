package com.fet.lineBot.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fet.lineBot.domain.dao.MangaDataRepository;
import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.domain.model.MangaData;
import com.fet.lineBot.service.ClampService;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

@Service
public class ClampServiceImpl implements ClampService {

	private static final Logger logger = LogManager.getLogger(ClampServiceImpl.class);

	private static FBPostData CACHED_DATA = null;
	
	
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
	
	@Override
	public String queryVoteResult() {
		String rtnMsg = "";
		try {
			WebClient webClient = new WebClient();
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setRedirectEnabled(true);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setTimeout(10000);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
			webClient.getOptions().setDoNotTrackEnabled(true);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			HtmlPage htmlPage = webClient
					.getPage("https://www.cec.gov.tw/pc/zh_TW/P1/n00000000000000000.html");
			webClient.waitForBackgroundJavaScript(JS_TIME);

//			logger.info(htmlPage.asXml());
			HtmlElement elements = htmlPage.getDocumentElement();
			List<HtmlTableRow> elementList = htmlPage.getByXPath( "//tr[@class='trT']");
			List<HtmlTableRow> footer = htmlPage.getByXPath( "//tr[@class='trFooterT']");
//			logger.info(new Gson().toJson(elementList));
			logger.info("Table Row: " + elementList.size());
//			投開票所數　已送/應送: 42/17226
			logger.info(footer.get(0).getCell(0).asText());
			String voteBox = footer.get(0).getCell(0).asText();
			logger.info(voteBox);
			logger.info(voteBox.split(" ").length);
			String[] boxsplit= voteBox.split(" ");
//			for(String box:boxsplit) {
//				logger.info(box);
//			}
			String[] ticketBoxs =  voteBox.split(" ")[1].split("/");
			
//			for(String box:ticketBoxs) {
//				logger.info("count: " + box);
//			}
			
			int hanCount;
			int thasCount;
			StringBuffer sb = new StringBuffer();
//			logger.info(elementList.get(0).getCell(1).asText());
//			logger.info(elementList.get(0).getCell(2).asText());
//			logger.info(elementList.get(0).getCell(4).asText());
//			logger.info(elementList.get(0).getCell(5).asText());
			
			// 宋楚瑜
			sb.append(elementList.get(0).getCell(1).asText());
			sb.append("/");
			sb.append(elementList.get(0).getCell(2).asText().substring(0,3));
			sb.append("/");
			sb.append(elementList.get(0).getCell(4).asText());
			sb.append("/");
			sb.append(elementList.get(0).getCell(5).asText());
			sb.append("%\n");
			
//			logger.info(elementList.get(1).getCell(1).asText());
//			logger.info(elementList.get(1).getCell(2).asText());
//			logger.info(elementList.get(1).getCell(4).asText());
//			logger.info(elementList.get(1).getCell(5).asText());
			
			// 韓國瑜
			sb.append(elementList.get(1).getCell(1).asText());
			sb.append("/");
			sb.append(elementList.get(1).getCell(2).asText().substring(0,3));
			sb.append("/");
			sb.append(elementList.get(1).getCell(4).asText());
			
			sb.append("/");
			sb.append(elementList.get(1).getCell(5).asText());
			sb.append("%\n");

//			logger.info(elementList.get(2).getCell(1).asText());
//			logger.info(elementList.get(2).getCell(2).asText());
//			logger.info(elementList.get(2).getCell(4).asText());
//			logger.info(elementList.get(2).getCell(5).asText());
			
			// 蔡英文
			sb.append(elementList.get(2).getCell(1).asText());
			sb.append("/");
			sb.append(elementList.get(2).getCell(2).asText().substring(0,3));
			sb.append("/");
			sb.append(elementList.get(2).getCell(4).asText());
			sb.append("/");
			sb.append(elementList.get(2).getCell(5).asText());
			sb.append("%\n");
			
			hanCount = Integer.valueOf(elementList.get(1).getCell(4).asText().replaceAll(",", ""));
			thasCount = Integer.valueOf(elementList.get(2).getCell(4).asText().replaceAll(",", ""));
			
			sb.append("\n總機先生目前贏 " + (hanCount - thasCount) +" 張選票!\n");
			sb.append("總統票剩餘Box: " + (Integer.valueOf(ticketBoxs[1].substring(0, 5)) - Integer.valueOf(ticketBoxs[0].trim())) + "\n");
			// HtmlTextInput account = (HtmlTextInput) htmlPage.getElementById("ACCOUNT");
			// account.setText(conf.userName);
			// HtmlPasswordInput passwd = (HtmlPasswordInput)
			// htmlPage.getElementById("PASSWORD");
			// passwd.setText(conf.passwd);
			// htmlPage.executeJavaScript("login()");
			// webClient.waitForBackgroundJavaScript(1500);
			// HtmlPage htmlPage2 = (HtmlPage)
			// htmlPage.getWebClient().getCurrentWindow().getEnclosedPage();
			//
			// DomElement priceList = htmlPage2.getElementById("bidprice");
			//
			// HtmlSelect select = (HtmlSelect) htmlPage2.getElementById("bidprice");
			// HtmlOption option = select.getOption(pricePos);
			// if (Integer.valueOf(target.getPriceLimited()) >
			// Integer.valueOf(option.asText())) {
			// select.setSelectedAttribute(option, true);
			// logger.info("select Change: " + priceList.asXml());
			// DomElement bidButton = htmlPage2.getElementById("bidButton");
			// HtmlPage htmlPage3 = bidButton.click();
			// logger.info("bid: " + htmlPage3.asXml());
			// webClient.close();
			// } else {
			// logger.info("targetPrice: " + option.asText() + " is overLimited. pass");
			// webClient.close();
			//
			// }
			htmlPage = webClient
					.getPage("https://www.cec.gov.tw/pc/zh_TW/L4/n00000000000000000.html");
			webClient.waitForBackgroundJavaScript(JS_TIME);
			elementList = htmlPage.getByXPath( "//tr[@class='trT']");
//			for(HtmlTableRow row: elementList) {
//				logger.info(row.getCell(0).asText());
//				logger.info(row.getCell(1).asText());
//				logger.info(row.getCell(2).asText());
//				logger.info(row.getCell(3).asText());
//			}
			footer = htmlPage.getByXPath( "//tr[@class='trFooterT']");
			ticketBoxs =  voteBox.split(" ")[1].split("/");
			sb.append("\n");
			sb.append(elementList.get(5).getCell(1).asText());
			sb.append("/");
			sb.append(elementList.get(5).getCell(3).asText());
			sb.append("%\n");
			sb.append(elementList.get(8).getCell(1).asText());
			sb.append("/");
			sb.append(elementList.get(8).getCell(3).asText());
			sb.append("%\n");
			sb.append(elementList.get(13).getCell(1).asText());
			sb.append("/");
			sb.append(elementList.get(13).getCell(3).asText());
			sb.append("%\n");
			sb.append(elementList.get(14).getCell(1).asText());
			sb.append("/");
			sb.append(elementList.get(14).getCell(3).asText());
			sb.append("%\n");
			sb.append("政黨票剩餘Box: " + (Integer.valueOf(ticketBoxs[1].substring(0, 5)) - Integer.valueOf(ticketBoxs[0].trim())) + "\n");
			sb.append("\n心存善念，盡力而為");
			webClient.close();
			rtnMsg = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rtnMsg;
	}

	public List<String> queryAnotherSide(int storyNum) {
		String baseUrl = "https://manmankan.cc/manhua/41287/";
		List<String> pictureUrl =null ;
		try {
			List<MangaData> mangaList = mangaDataRepository.findByMangaIdAndChapterNo(1, storyNum);
			if(mangaList.size()<1) {
				List<String> chapterList =  getTopics(baseUrl);
				pictureUrl = getPicturs(chapterList.get(storyNum));
				mangaList = new ArrayList<MangaData>();
				for(int i=0;i<pictureUrl.size();i++) {
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

	private List<String> getTopics(String baseUrl) throws IOException, MalformedURLException {
		WebClient webClient = getWebClient();
		HtmlPage htmlPage = webClient
				.getPage(baseUrl);
		webClient.waitForBackgroundJavaScript(JS_TIME);

//		logger.info(htmlPage.getElementById("mh-chapter-list-ol-0").asXml());
		List<DomElement> aList = htmlPage.getByXPath("//ul[@id='mh-chapter-list-ol-0']/li/a");
//		logger.info(aList.get(0).getAttribute("href"));
		List<String> urlList = aList.stream().map(element -> "https://manmankan.cc" + element.getAttribute("href")).collect(Collectors.toList());
		
		webClient.close();
		return urlList;
	}
	
	private List<String> getPicturs(String url) throws IOException, MalformedURLException {
		WebClient webClient = getJSWebClient();
		List<String> imageUrl = new ArrayList<String>();
		for(int i=1;i<30;i++) {
		HtmlPage htmlPage = webClient
				.getPage(url + "#@page=" + i);
			webClient.waitForBackgroundJavaScript(JS_TIME);
//			logger.info("IMGUel" +  htmlPage.getElementByName("page_1").getAttribute("src"));
			String imgUrl = htmlPage.getElementByName("page_1").getAttribute("src");
			if(imgUrl.contains("undefined")) {
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
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setRedirectEnabled(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setTimeout(10000);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
		webClient.getOptions().setDoNotTrackEnabled(true);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		return webClient;
	}

  @Override
  public FBPostData queryFBNewestPost() {
    if(null==CACHED_DATA) {      
      try {
        getNewestPostBySchedule();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return CACHED_DATA;
  }

  @Scheduled(initialDelay = 120000, fixedRate = 120000)
  private void getNewestPostBySchedule() throws IOException {
    WebClient webClient = getJSWebClient();
    try {
      HttpResponse<String> responses = Unirest.get(checkPage).asString();
      String body = responses.getBody();
      body =  body.substring(9);
           
      JSONObject jsonObj = new JSONObject( body);
      JSONArray array = jsonObj.getJSONArray("actions");
      String html = array.getJSONObject(0).getString("html");
      URL url = new URL("http://www.example.com");
      StringWebResponse response = new StringWebResponse("<html><head><title>Test</title></head><body>" + html +"</body></html>", url);
      WebClient client = new WebClient();
      HtmlPage page = HTMLParser.parseHtml(response, client.getCurrentWindow());
      /* 切出包含貼文的 DIV */
      List<DomElement> bodyDivList = page.getBody().getByXPath("./div/div/div/div/div");
      List<DomElement> elementList = bodyDivList.stream().filter(item ->{
        DomElement dom = (DomElement) item;
        return dom.getByXPath("./div[@class=\"story_body_container\"]").size()>0;
      }).collect(Collectors.toList());
      FBPostData data = new FBPostData();
      for(DomElement element:elementList) {
        /* 切出包含設定檔中 hashTag 的相關貼文 */
        if(element.getByXPath("./div/div/span/p/a/span").stream().filter((item -> {
          DomElement ele = (DomElement)item;
          return checkHashTeg.equalsIgnoreCase(ele.getTextContent());
        })).count()>0) {
          /* 處理貼文 ID */
          String storyId = null;
          List<DomElement> storyIdList = element.getByXPath("./div/div/a");
          if(storyIdList.size()>0) {
            String href = storyIdList.get(0).getAttribute("href");
            storyId = href.substring(href.indexOf("=") +1, href.indexOf("&"));
          }
          
          /* 觀察到新貼文時建立快取圖片路徑 */
          if(data.getStoryId()<Long.valueOf(storyId)) {
            
            String imgUrl = null;
            /* 切出圖片路徑 */
            List<DomElement> imgList = element.getByXPath("./div/div/div/a/img");
            if(imgList.size()>0) {
              imgUrl = imgList.get(0).getAttribute("src");
            }
            data.setStoryId(Long.valueOf(storyId));
            data.setImgUrl(imgUrl);
          
          }
        }
      }
      logger.info(new Gson().toJson(data));

      if(null!=CACHED_DATA) {
        /* 更換暫存資料並發送 Line 通知 */
        if(data.getStoryId()>CACHED_DATA.getStoryId()) {
          CACHED_DATA = data;
          sendNotify();
        }
      }else {
        CACHED_DATA = data;
      }
    } catch (FailingHttpStatusCodeException e) {
      logger.error(e);
    } finally {
      webClient.close();
    }
    System.gc();
  }

  
  private void sendNotify() {
    HttpResponse<String> response = Unirest.post("https://notify-api.line.me/api/notify")
        .header("Authorization", "Bearer " + token).multiPartContent()
        .field("message", "https://www.facebook.com/Wishswing/posts/" + CACHED_DATA.getStoryId())
        .field("imageFullsize", CACHED_DATA.getImgUrl())
        .field("imageThumbnail", CACHED_DATA.getImgUrl())
        .asString();
    logger.info(response.getBody());
  }
  
  
  @Scheduled(initialDelay = 120000, fixedRate = 1200000)
  private void renewHeroku() {
    logger.info("heartbeat");
    HttpResponse<String> response = Unirest.get(selfLocation).asString();
    logger.info(response.getBody());
  }
}
