package com.fet.lineBot.domain.dao;

import com.fet.lineBot.domain.model.MemberData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MemberDataRepository extends CrudRepository<MemberData, Long> {
  List<MemberData> findAll();

  MemberData findByUserId(String userId);
}
