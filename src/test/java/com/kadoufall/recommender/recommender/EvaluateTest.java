package com.kadoufall.recommender.recommender;

import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EvaluateTest {
    @Test
    public void testUserCF() throws Exception {
        DataModel model = new GenericBooleanPrefDataModel(
                GenericBooleanPrefDataModel.toDataMap(
                        new FileDataModel(new File("src/main/resources/caixin.base"))));

        RecommenderIRStatsEvaluator evaluator =
                new GenericRecommenderIRStatsEvaluator();
//        UserSimilarity similarity = new TanimotoCoefficientSimilarity(com.kadoufall.model);
        UserSimilarity similarity = new LogLikelihoodSimilarity(model);
        UserNeighborhood neighborhood =
                new NearestNUserNeighborhood(10, similarity, model);
        RecommenderBuilder recommenderBuilder = model1 -> {
            return new GenericBooleanPrefUserBasedRecommender(model1, neighborhood, similarity);
        };
        DataModelBuilder modelBuilder = trainingData -> new GenericBooleanPrefDataModel(
                GenericBooleanPrefDataModel.toDataMap(trainingData));
        IRStatistics stats = evaluator.evaluate(
                recommenderBuilder, modelBuilder, model, null, 10,
                GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,
                1.0);
        System.out.println(stats.getPrecision());
        System.out.println(stats.getRecall());
        System.out.println(stats.getF1Measure());
    }
}
