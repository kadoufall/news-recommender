package com.kadoufall.recommender.service.hot;

import com.kadoufall.recommender.dao.NewsRepository;
import com.kadoufall.recommender.dao.UserOperationRepository;
import com.kadoufall.recommender.dao.UtilRepositoryCustom;
import com.kadoufall.recommender.model.News;
import com.kadoufall.recommender.model.UserOperation;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j
@Service
public class HotRecommenderServiceImpl implements HotRecommenderService {

    @Value("${com.kadoufall.recommender.recommendNum}")
    private int recommendNum;

    @Value("${com.kadoufall.recommender.nowStr}")
    private String nowStr;

    @Value("${com.kadoufall.recommender.hotDays}")
    private int hotDays;

    private Timestamp beginTime;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private UtilRepositoryCustom utilRepositoryCustom;

    @Autowired
    private UserOperationRepository userOperationRepository;

    private List<News> hottestNews;

    public HotRecommenderServiceImpl() {

    }

    @PostConstruct
    public void init() {
//        this.hottestNews = this.newsRepository.findHostestNews(PageRequest.of(0, this.recommendNum));
        this.hottestNews = this.newsRepository.findHostestNews();

        LocalDateTime now = LocalDateTime.parse(this.nowStr, DateTimeFormatter.ofPattern("yyyy.MM.dd_HH:mm:ss"));
        this.beginTime = Timestamp.valueOf(now.minusDays(this.hotDays));
    }

    @Override
    public List<Long> recommend(long userId) {
        List<Long> seenNews = this.utilRepositoryCustom.findSeenNewsIds(userId);

        return this.hottestNews.stream().filter(news -> {
            boolean isOutdated = news.getPostTime().before(this.beginTime);
            if (isOutdated) {
                return false;
            }
            boolean seen = seenNews.contains(news.getId());
            return !seen;
        }).limit(this.recommendNum).map(News::getId).collect(Collectors.toList());
    }

    @Override
    public List<News> recommendNews(long userId) {


        return this.recommend(userId).parallelStream().map(newsId -> {
            return this.newsRepository.findById(newsId.longValue());
        }).collect(Collectors.toList());

    }

    private List<Long> recommendWithNum(long userId, int num) {
        List<Long> seenNewsAll = this.utilRepositoryCustom.findSeenNewsIdsBefore(userId, this.beginTime);
        return this.hottestNews.stream().filter(news -> {
            boolean isOutdated = news.getPostTime().before(this.beginTime);
            if (isOutdated) {
                return false;
            }
            boolean seen = seenNewsAll.contains(news.getId());
            return !seen;
        }).limit(num).map(News::getId).collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> evaluate() {
        List<UserOperation> testUserOperation = this.userOperationRepository.findAll()
                .parallelStream()
                .filter(userOperation -> userOperation.getReadTime().after(this.beginTime))
                .collect(Collectors.toList());

        Map<Long, List<Long>> testData = new ConcurrentHashMap<>();
        testUserOperation.parallelStream().forEach(u -> {
            long userId = u.getUserId();
            if (testData.containsKey(userId)) {
                testData.get(userId).add(u.getNewsId());
            } else {
                testData.put(userId, new ArrayList<Long>() {{
                    this.add(u.getNewsId());
                }});
            }
        });


        List<Map<String, Double>> all = IntStream.range(1, 31).parallel().boxed().map(i -> {
            System.out.println(i);
            Map<String, Double> ret = new HashMap<>(16);

            int hit = 0;
            int allPrecision = 0;
            int allRecall = 0;

            for (Map.Entry<Long, List<Long>> entry : testData.entrySet()) {
                long userId = entry.getKey();
                List<Long> testNewsIds = entry.getValue();

                List<Long> recommendNewsIds = this.recommendWithNum(userId, i);

//            if (testNewsIds.size() < 5) {
//                recommendNewsIds = this.recommendWithNum(userId, 5);
//            } else if(testNewsIds.size() < 10)  {
//                recommendNewsIds = this.recommendWithNum(userId, 10);
//            } else if(testNewsIds.size() < 20)  {
//                recommendNewsIds = this.recommendWithNum(userId, 20);
//            }  else if(testNewsIds.size() <= 50)  {
//                recommendNewsIds = this.recommendWithNum(userId, 50);
//            } else if(testNewsIds.size() <= 75)  {
//                recommendNewsIds = this.recommendWithNum(userId, 75);
//            } else {
//                recommendNewsIds = this.recommendWithNum(userId, 100);
//            }

                allPrecision += recommendNewsIds.size();
                allRecall += testNewsIds.size();
                recommendNewsIds.retainAll(testNewsIds);
                hit += recommendNewsIds.size();
            }

            double precision = hit * 1.0 / allPrecision;
            double recall = hit * 1.0 / allRecall;
            double fScore = 2 * recall * precision / ((recall + precision) * 1.0);

            ret.put("Precision", precision);
            ret.put("Recall", recall);
            ret.put("F1Measure", fScore);
            System.out.println(precision + " " + recall + " " + fScore);
            return ret;
        }).collect(Collectors.toList());

        try {
            FileOutputStream fos = new FileOutputStream("src/main/resources/hot.txt");
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
