package com.kadoufall.recommender.web;

import com.kadoufall.recommender.dao.NewsRepository;
import com.kadoufall.recommender.model.News;
import com.kadoufall.recommender.service.hot.HotRecommenderService;
import com.kadoufall.recommender.service.userCF.UserCFRecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/recommend")
public class RecommendController {
    @Autowired
    private UserCFRecommenderService userCFRecommenderService;
    private HotRecommenderService hotRecommenderService;
    private NewsRepository newsRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "Recommender for Todayim";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public List<News> getRecommendNews(@PathVariable long id) {
        return userCFRecommenderService.recommendNews(id);
    }
}
