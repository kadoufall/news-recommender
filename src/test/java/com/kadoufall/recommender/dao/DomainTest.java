package com.kadoufall.recommender.dao;

import com.kadoufall.recommender.model.News;
import com.kadoufall.recommender.model.User;
import com.kadoufall.recommender.model.UserOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DomainTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private UserOperationRepository userOperationRepository;

    @Test
    public void testUser() throws Exception {

        List<User> users = userRepository.findAll();
        for (User user:users) {
            System.out.println(user.getId());
        }
    }

    @Test
    public void testNews() throws Exception {
        List<News> newsList = newsRepository.findAll();
        for (News news:newsList) {
            System.out.println(news.getId() + " " + news.getTitle() + " " + news.getPostTime());
        }

    }

    @Test
    public void testUserOperation() throws Exception {
        List<UserOperation> userOperations = userOperationRepository.findAll();

        for (UserOperation useroperation:userOperations) {
            System.out.println(useroperation.getId() +" " + useroperation.getNewsId() + " "
            + useroperation.getUserId() + " " + useroperation.getReadTime());
        }
    }
}
