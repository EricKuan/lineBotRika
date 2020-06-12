package lineBot;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
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
	  HttpResponse<String> response = Unirest.get("https://m.facebook.com/page_content_list_view/more/?page_id=542665685764290&start_cursor=1&num_to_fetch=20&surface_type=timeline")
          .asString();
	  System.out.println(response.getBody());
	  String body = response.getBody();
	  String[] splits = body.split("story_fbid=");
	  List<String> postIdList = new ArrayList<String>();
	  for(String str:splits) {
	    String postId = str.substring(0, str.indexOf("&"));
	    if(!postIdList.contains(postId)) {
	      postIdList.add(postId);
	    }
	  }
	  System.out.println(new Gson().toJson(postIdList));
	  
    }
	

}
