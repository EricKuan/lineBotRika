package com.fet.lineBot.domain.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.fet.lineBot.domain.model.ReplyMapping;

public interface  ReplyRepository extends CrudRepository<ReplyMapping, Long>{
	List<ReplyMapping> findAll();
	
	List<ReplyMapping> findByMessage(String message);
	
	long deleteByMessage(String message);

}
