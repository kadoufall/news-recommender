package com.kadoufall.recommender.recommender;

import com.kadoufall.recommender.service.userCF.UserCFRecommenderService;
import com.kadoufall.recommender.service.contentBased.NewsItemSimilarity;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MahoutTest {

    @Autowired
    private UserCFRecommenderService userCFRecommenderService;

    @Test
    public void testUserCF() throws Exception {
        for (int i = 5; i <= 30; i += 5) {
            if (i == 10) {
                continue;
            }
            userCFRecommenderService.evaluate(i, "TanimotoCoefficientSimilarity");
        }

        for (int i = 5; i <= 30; i += 5) {
            if (i == 10) {
                continue;
            }
            userCFRecommenderService.evaluate(i, "CityBlockSimilarity");
        }


//        Map<String, Double> result = userCFRecommenderService.evaluate(10);

//        for (Map.Entry<String, Double> entry : result.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }
    }

}
