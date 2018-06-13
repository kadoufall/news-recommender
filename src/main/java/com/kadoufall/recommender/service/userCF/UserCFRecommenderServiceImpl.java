package com.kadoufall.recommender.service.userCF;

import com.kadoufall.recommender.dao.NewsRepository;
import com.kadoufall.recommender.dao.UserOperationRepository;
import com.kadoufall.recommender.model.News;
import lombok.extern.log4j.Log4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j
@Service
public class UserCFRecommenderServiceImpl implements UserCFRecommenderService {

    @Value("${com.kadoufall.mahout.table-name}")
    private String tableName;

    @Value("${com.kadoufall.mahout.user-column}")
    private String userColumn;

    @Value("${com.kadoufall.mahout.item-column}")
    private String itemColumn;

    @Value("${com.kadoufall.mahout.pref-column}")
    private String prefColumn;

    @Value("${com.kadoufall.mahout.recommendNum}")
    private int recommendNum;

    @Autowired
    DataSource dataSource;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    UserOperationRepository userOperationRepository;

    private DataModel dataModel = null;
    private Recommender recommender = null;

    public UserCFRecommenderServiceImpl() {

    }

    @PostConstruct
    public void init() {
        try {
           this.dataModel = new MySQLBooleanPrefJDBCDataModel(
                   dataSource,
                   this.tableName,
                   this.userColumn,
                   this.itemColumn,
                   this.prefColumn
           );
            UserSimilarity similarity = new LogLikelihoodSimilarity(dataModel);
            // UserSimilarity similarity = new TanimotoCoefficientSimilarity(dataModel);
            // UserSimilarity similarity = new CityBlockSimilarity(dataModel);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, dataModel);
            this.recommender = new GenericBooleanPrefUserBasedRecommender(dataModel, neighborhood, similarity);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 评估基于用户的协同过滤推荐
     *
     * @return map，key为Precision, Recall, F1Measure
     */
    @Override
    public Map<String, Double> evaluate(int neighborhoodNum, String method) {
//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "2");

        RecommenderIRStatsEvaluator evaluator =
                new GenericRecommenderIRStatsEvaluator();
        RecommenderBuilder recommenderBuilder = model1 -> {
            UserSimilarity similarity = null;
            if ("TanimotoCoefficientSimilarity".equals(method)) {
                similarity = new TanimotoCoefficientSimilarity(model1);
            } else if ("CityBlockSimilarity".equals(method)) {
                similarity = new CityBlockSimilarity(model1);
            }
//            UserSimilarity similarity = new TanimotoCoefficientSimilarity(model1);
//            UserSimilarity similarity = new CityBlockSimilarity(model1);
            UserNeighborhood neighborhood =
                    new NearestNUserNeighborhood(neighborhoodNum, similarity, model1);
            return new GenericBooleanPrefUserBasedRecommender(model1, neighborhood, similarity);
        };
        DataModelBuilder modelBuilder = trainingData -> new GenericBooleanPrefDataModel(
                GenericBooleanPrefDataModel.toDataMap(trainingData));
        try {
            List<Map<String, Double>> all = IntStream.range(1, 31).parallel().boxed().map(i -> {
                System.out.println(i);
                Map<String, Double> ret = new HashMap<>(16);
                try {
                    IRStatistics stats = evaluator.evaluate(
                            recommenderBuilder, modelBuilder, this.dataModel, null, i,
                            GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,
                            1.0);

                    ret.put("Precision", stats.getPrecision());
                    ret.put("Recall", stats.getRecall());
                    ret.put("F1Measure", stats.getF1Measure());
                } catch (TasteException e) {
                    log.error(e.getMessage());
                }

                return ret;
            }).collect(Collectors.toList());

            String path = "";
            if ("TanimotoCoefficientSimilarity".equals(method)) {
                path = "src/main/resources/Tanimoto-" + String.valueOf(neighborhoodNum) + ".txt";
            } else if ("CityBlockSimilarity".equals(method)) {
                path = "src/main/resources/City-" + String.valueOf(neighborhoodNum) + ".txt";
            }

            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");

            for (int key = 0; key < all.size(); key++) {

                System.out.println();
                log.info(key + 1);

                double Precision = all.get(key).get("Precision");
                double Recall = all.get(key).get("Recall");
                double F1Measure = all.get(key).get("F1Measure");

                String write = "|" + String.valueOf(key + 1) + "|" + String.valueOf(Precision) + "|"
                        + String.valueOf(Recall) + "|" + String.valueOf(F1Measure) + "|" + "\n";
                osw.write(write);


                for (Map.Entry<String, Double> index : all.get(key).entrySet()) {
                    log.info(index.getKey() + " " + index.getValue());
                }

                System.out.println();
            }


            osw.close();
            fos.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 向用户推荐新闻
     *
     * @param userId 用户id
     * @return 新闻id的list
     */
    @Override
    public List<Long> recommend(long userId) {
        List<Long> ret = new ArrayList<>();

        try {
            List<RecommendedItem> recommendedItems = recommender.recommend(userId, this.recommendNum);

            Set<Long> newsIds = new HashSet<>();
            for (RecommendedItem item : recommendedItems) {
                newsIds.add(item.getItemID());
            }
            ret.addAll(newsIds);
        } catch (TasteException e) {
            log.error(e.getMessage());
        }

        return ret;
    }

    @Override
    public List<News> recommendNews(long userId) {
        return this.recommend(userId)
                .parallelStream()
                .map(newsId -> this.newsRepository.findById(newsId.longValue()))
                .collect(Collectors.toList());
    }


}
