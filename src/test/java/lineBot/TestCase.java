package lineBot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.fet.lineBot.domain.model.FBPostData;
import com.fet.lineBot.service.impl.ClampServiceImpl;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.google.gson.Gson;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class)
//@WebAppConfiguration
public class TestCase {
	private static final Logger logger = LogManager.getLogger(ClampServiceImpl.class);

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
		HttpResponse<String> response = Unirest.get(
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

		HttpResponse<String> responses = Unirest.get(
				"https://m.facebook.com/page_content_list_view/more/?page_id=542665685764290&start_cursor=1&num_to_fetch=20&surface_type=timeline")
				.asString();
		String body = responses.getBody();
		body = body.substring(9);

		System.out.println(body);
		JSONObject jsonObj = new JSONObject(body);
		JSONArray array = jsonObj.getJSONArray("actions");
		System.out.println(array.get(0));
		String html = array.getJSONObject(0).getString("html");
		System.out.println(html);
		URL url = new URL("http://www.example.com");
		StringWebResponse response = new StringWebResponse(
				"<html><head><title>Test</title></head><body>" + html + "</body></html>", url);
		WebClient client = new WebClient();
		HtmlPage page = HTMLParser.parseHtml(response, client.getCurrentWindow());
		System.out.println(page.getTitleText());
//      System.out.println(page.asXml());
		logger.info(page.asXml());
		List<DomElement> bodyDivList = page.getBody().getByXPath("./div/div/div/div/div");
		List<DomElement> elementList = bodyDivList.stream().filter(item -> {
			DomElement dom = (DomElement) item;
			return dom.getByXPath("./div[@class=\"story_body_container\"]").size() > 0;
		}).collect(Collectors.toList());
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
			if (data.getStoryId() < Long.valueOf(storyId)) {

				String imgUrl = null;
				/* 切出圖片路徑 */
				List<DomElement> imgList = element.getByXPath("./div/div/div/a/img");
				if (imgList.size() > 0) {
					imgUrl = imgList.get(0).getAttribute("src");
				}
				data.setStoryId(Long.valueOf(storyId));
				data.setImgUrl(imgUrl);

			}

		}
		logger.info(new Gson().toJson(data));
	}
	@Test
	public void test06() throws JSONException, IOException {
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
		String baseUrl = "https://www.wenku8.net/novel/1/1695/";
		webClient.waitForBackgroundJavaScript(500);
		HtmlPage page = webClient.getPage(baseUrl);
		logger.info(page.asXml());
		List<DomElement> hrefList = page.getBody().getByXPath("//td[@class=\"ccss\"]/a");
		StringBuffer sb = new StringBuffer();
		for(DomElement elem:hrefList) {
			String chapterUrl = baseUrl + elem.getAttribute("href");
			//logger.info(chapterUrl);
			HtmlPage chapterPage = webClient.getPage(chapterUrl);
			webClient.waitForBackgroundJavaScript(500);
			//logger.info(chapterPage.asXml());
			DomElement content = (DomElement) chapterPage.getBody().getByXPath("//div[@id=\"content\"]").stream().findFirst().get();
			String original = content.asText();
			String translation = ZhConverterUtil.convertToTraditional(original);
			sb.append(translation);
		}
		FileOutputStream fo = new FileOutputStream(new File("F:\\noval\\騎士與魔法.txt"));
		fo.write(sb.toString().getBytes());
		fo.flush();
		fo.close();
		webClient.close();
	}
	
	
	@Test
	public void test07() throws JSONException, IOException {
		ClampServiceImpl service = new ClampServiceImpl();
		String msg = service.getNovel(1695);
		System.out.println(msg);
	}
}
