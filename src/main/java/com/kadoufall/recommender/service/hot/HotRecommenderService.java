package com.kadoufall.recommender.service.hot;

import com.kadoufall.recommender.model.News;

import java.util.List;
import java.util.Map;

public interface HotRecommenderService {
    List<Long> recommend(long userId);

    List<News> recommendNews(long userId);
    Map<String, Double> evaluate();

}
