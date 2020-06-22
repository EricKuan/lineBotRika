package com.fet.lineBot.domain.dao;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.fet.lineBot.domain.model.MangaData;

public interface  MangaDataRepository extends CrudRepository<MangaData, Long>{
	List<MangaData> findAll();
	
	List<MangaData> findByMangaIdAndChapterNo(int mangaId, int chapterNo);
	

}
