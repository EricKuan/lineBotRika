package com.fet.lineBot.domain.dao;

import com.fet.lineBot.domain.model.MangaData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MangaDataRepository extends CrudRepository<MangaData, Long> {
  List<MangaData> findAll();

  List<MangaData> findByMangaIdAndChapterNo(int mangaId, int chapterNo);
}
