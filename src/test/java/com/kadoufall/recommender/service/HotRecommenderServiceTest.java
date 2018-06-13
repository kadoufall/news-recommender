package com.kadoufall.recommender.service;

import com.kadoufall.recommender.service.hot.HotRecommenderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HotRecommenderServiceTest {
    @Autowired
    HotRecommenderService hotRecommenderService;

    @Test
    public void testHotNum() throws Exception {
//        this.hotRecommenderService.recommend(5218791);
//        this.hotRecommenderService.evaluate();
    }

}
