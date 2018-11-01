package com.kadoufall.recommender.service.contentBased;

import com.kadoufall.recommender.dao.UserOperationRepository;
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
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
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
public class ContentBasedRecommenderServiceImpl implements ContentBasedRecommenderService {
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
    UserOperationRepository userOperationRepository;

    private ParagraphVectors vectors = null;
    private DataModel dataModel = null;
    private Recommender recommender = null;

    public ContentBasedRecommenderServiceImpl() {

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

            // 注意需要先在 TrainVsmModel 中训练
            this.initParagraphVectors();

            // TODO: 移除 dataModel 中过期的用户浏览新闻行为，这些行为对计算用户相似度不再具有较大价值

            ItemSimilarity similarity = new NewsItemSimilarity(this.vectors);
            this.recommender = new GenericBooleanPrefItemBasedRecommender(dataModel, similarity);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void initParagraphVectors() throws Exception {
        ClassPathResource resource = new ClassPathResource("/model.pv");
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        this.vectors = WordVectorSerializer.readParagraphVectors(resource.getFile());
        vectors.setTokenizerFactory(t);
    }

    @Override
    public List<Long> recommend(long userId) {
        List<Long> ret = new ArrayList<>();

        try {
            List<RecommendedItem> recommendedItems = recommender.recommend(userId, this.recommendNum);
            ret = recommendedItems.parallelStream().map(RecommendedItem::getItemID).collect(Collectors.toList());
        } catch (TasteException e) {
            log.error(e.getMessage());
        }

        return ret;
    }

    @Override
    public Map<String, Double> evaluate() {
        RecommenderIRStatsEvaluator evaluator =
                new GenericRecommenderIRStatsEvaluator();
        RecommenderBuilder recommenderBuilder = model1 -> {
            ItemSimilarity similarity = new NewsItemSimilarity(this.vectors);
            return new GenericBooleanPrefItemBasedRecommender(model1, similarity);
        };
        DataModelBuilder modelBuilder = trainingData -> new GenericBooleanPrefDataModel(
                GenericBooleanPrefDataModel.toDataMap(trainingData));

        List<Map<String, Double>> all = IntStream.range(1, 2).parallel().boxed().map(i -> {
            System.out.println(i);
            Map<String, Double> ret = new HashMap<>(16);

            try {
                IRStatistics stats = evaluator.evaluate(
                        recommenderBuilder, modelBuilder, this.dataModel, null, i,
                        GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,
                        1.0);

//                System.out.println(stats.getPrecision());
//                System.out.println(stats.getRecall());
//                System.out.println(stats.getF1Measure());

                ret.put("Precision", stats.getPrecision());
                ret.put("Recall", stats.getRecall());
                ret.put("F1Measure", stats.getF1Measure());
            } catch (TasteException e) {
                log.error(e.getMessage());
            }

            return ret;
        }).collect(Collectors.toList());

        try {
            FileOutputStream fos = new FileOutputStream("src/main/resources/contentBased.txt");
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

}
