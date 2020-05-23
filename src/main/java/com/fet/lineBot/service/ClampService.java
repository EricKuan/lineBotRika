package com.fet.lineBot.service;

import java.util.List;

public interface ClampService {

	public String queryVoteResult();
	
	public List<String> queryAnotherSide(int storyNum);
}
