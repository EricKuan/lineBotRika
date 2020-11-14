package com.fet.lineBot.domain.dao;

import com.fet.lineBot.domain.model.BonusPhotoData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BonusPhotoDataRepository extends CrudRepository<BonusPhotoData, Long> {
    List<BonusPhotoData> findAll();

    List<BonusPhotoData> findByUserId(String userId);

    @Query(value = "SELECT id, USER_ID, LINE_NAME, PIECE_NAME, LINE_NAME, CHARACTER_NAME, Create_Date FROM BONUS_VOTE_DATA WHERE date_part('month', Create_Date) = :month ",
            nativeQuery = true)
    List<BonusPhotoData> findByDate(@Param("month") int month);

}
