package com.kadoufall.recommender.service;

import com.kadoufall.recommender.service.contentBased.ContentBasedRecommenderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ContentBasedRecommenderServiceTest {
    @Autowired
    ContentBasedRecommenderService contentBasedRecommenderService;

    @Test
    public void testHotNum() throws Exception {
        this.contentBasedRecommenderService.evaluate();
    }
}
