package com.kadoufall.recommender.dao;

import com.kadoufall.recommender.model.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    News findById(long id);



    @Query("from News ORDER BY viewNum DESC")
//    List<News> findHostestNews(Pageable pageable);
    List<News> findHostestNews();



}