package com.fet.lineBot.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fet.lineBot.service.ClampService;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.google.gson.Gson;

@Service
public class ClampServiceImpl implements ClampService {

	private static final Logger logger = LogManager.getLogger(ClampServiceImpl.class);

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
					.getPage("https://db.cec.gov.tw/histQuery.jsp?voteCode=20160101P1A1&qryType=ctks");
			webClient.waitForBackgroundJavaScript(1500);

//			logger.info(htmlPage.asXml());
			HtmlElement elements = htmlPage.getDocumentElement();
			List<HtmlTableRow> elementList = htmlPage.getByXPath( "//tr[@class='data']");
//			logger.info(new Gson().toJson(elementList));
			logger.info("Table Row: " + elementList.size());
			StringBuffer sb = new StringBuffer();
			logger.info(elementList.get(0).getCell(1).asText());
			logger.info(elementList.get(0).getCell(4).asText());
			logger.info(elementList.get(0).getCell(5).asText());
			logger.info(elementList.get(0).getCell(6).asText());
			
			sb.append(elementList.get(0).getCell(1).asText());
			sb.append("/");
			sb.append(elementList.get(0).getCell(5).asText());
			sb.append("/");
			sb.append(elementList.get(0).getCell(6).asText());
			sb.append("/");
			sb.append(elementList.get(0).getCell(7).asText());
			sb.append("\n");
			
			logger.info(elementList.get(2).getCell(0).asText());
			logger.info(elementList.get(2).getCell(4).asText());
			logger.info(elementList.get(2).getCell(5).asText());
			logger.info(elementList.get(2).getCell(6).asText());
			sb.append(elementList.get(2).getCell(0).asText());
			sb.append("/");
			sb.append(elementList.get(2).getCell(4).asText());
			sb.append("/");
			sb.append(elementList.get(2).getCell(5).asText());
			sb.append("/");
			sb.append(elementList.get(2).getCell(6).asText());
			sb.append("\n");

			logger.info(elementList.get(4).getCell(0).asText());
			logger.info(elementList.get(4).getCell(4).asText());
			logger.info(elementList.get(4).getCell(5).asText());
			logger.info(elementList.get(4).getCell(6).asText());
			sb.append(elementList.get(4).getCell(0).asText());
			sb.append("/");
			sb.append(elementList.get(4).getCell(4).asText());
			sb.append("/");
			sb.append(elementList.get(4).getCell(5).asText());
			sb.append("/");
			sb.append(elementList.get(4).getCell(6).asText());
			sb.append("\n");
			
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
			
			rtnMsg = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rtnMsg;
	}

}
