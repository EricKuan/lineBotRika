package com.fet.lineBot.domain.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.fet.lineBot.domain.model.MemberData;

public interface  MemberDataRepository extends CrudRepository<MemberData, Long>{
	List<MemberData> findAll();
	
	MemberData findByUserId(String userId);
	

}
