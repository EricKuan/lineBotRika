package lineBot;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import com.fet.lineBot.service.impl.ClampServiceImpl;
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
		String urlList = service.queryFBNewestPost();
		System.out.println(new Gson().toJson(urlList));
	}
	
	@Test
    public void test04() {
//	  HttpResponse<String> response = Unirest.post("https://notify-api.line.me/api/notify")
//	        .header("Authorization", "Bearer " + "Z9GcsPjrnaP8WHaiQnuJNEUS0zMArqFyLSHIiQw5MJI").multiPartContent().field("message", "ttest")
//	        .asString();
	  HttpResponse<String> response = Unirest.get("https://linebotrika.herokuapp.com/")
          .asString();
	  System.out.println(response.getBody());
    }
	
	   @Test
	    public void test05() {
	     WebDriver driver = new FirefoxDriver();
	     driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	     driver.get("https://www.google.com.tw/"); //開啟瀏覽器到 Google 首頁
	     JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
	     javascriptExecutor.executeScript("arguments[0].value='keyword';", "遠寶金融科技");
	   //印出十頁的所有搜尋結果Title和Link url
	     for (int i = 0; i < 10; i++) {
	      //抓取DOM elements, (.r a) 為Google搜尋結果的link
	      List<WebElement> searchReultATagList = driver.findElements(By.cssSelector(".r a"));
	      for (WebElement searchReultATag : searchReultATagList) {
	       System.out.println(searchReultATag.getText() + " : ");
	       System.out.println(searchReultATag.getAttribute("href"));
	       System.out.println("=======================");
	      }
	      //抓取DOM element, #pnnext 為Google搜尋下一頁按鈕
	      WebElement nextPageBtn = driver.findElement(By.id("pnnext"));
	      nextPageBtn.click();
	     }
	      
	     driver.quit(); //關閉瀏覽器
	   }
}
