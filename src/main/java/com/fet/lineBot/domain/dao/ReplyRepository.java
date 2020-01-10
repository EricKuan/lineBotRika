package com.fet.lineBot.domain.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.fet.lineBot.domain.model.ReplyMapping;

public interface  ReplyRepository extends CrudRepository<ReplyMapping, Long>{
	List<ReplyMapping> findAll();
	
	List<ReplyMapping> findByMessage(String message);
	
	@Modifying
	@Query("DELETE FROM LINE_BOT_REPLY_MAPPING L WHERE L.MESSAGE:=message")
	void deleteMessages(@Param("message")String message);

}
