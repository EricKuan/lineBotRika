package com.fet.lineBot.domain.dao;

import com.fet.lineBot.domain.model.BonusPhotoData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BonusPhotoDataRepository extends CrudRepository<BonusPhotoData, Long> {
  List<BonusPhotoData> findAll();

  List<BonusPhotoData> findByUserId(String userId);

}
