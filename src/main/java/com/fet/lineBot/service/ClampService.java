package com.fet.lineBot.service;

import java.util.List;

public interface ClampService {

	public String queryVoteResult();
	
	public List<String> queryAnotherSide(int storyNum);
	
	/**
	 * 查詢最新一話的 FB 連結
	 * @return
	 */
	public String queryFBNewestPost();
}
