package com.kadoufall.recommender.dao;

import com.kadoufall.recommender.model.News;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

public interface UtilRepositoryCustom {
    List<Long> findSeenNewsIds(long userId);

    List<Long> findSeenNewsIdsBefore(long userId, Timestamp timestamp);

}
