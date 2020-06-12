package com.fet.lineBot.service;

import java.util.List;
import com.fet.lineBot.domain.model.FBPostData;

public interface ClampService {

	public String queryVoteResult();
	
	public List<String> queryAnotherSide(int storyNum);
	
	/**
	 * 查詢最新一話的 FB 連結
	 * @return
	 */
	public FBPostData queryFBNewestPost();
}
