package com.fet.lineBot.domain.dao;

import com.fet.lineBot.domain.model.ReplyMapping;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReplyRepository extends CrudRepository<ReplyMapping, Long> {
  List<ReplyMapping> findAll();

  List<ReplyMapping> findByMessage(String message);
}
