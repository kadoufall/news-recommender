package com.kadoufall.recommender.service.contentBased;

import com.kadoufall.recommender.service.util.UtilService;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

public class NewsItemSimilarity implements ItemSimilarity {

    private ParagraphVectors vectors = null;

    public NewsItemSimilarity(ParagraphVectors vectors) {
        this.vectors = vectors;
    }

    @Override
    public double itemSimilarity(long itemID1, long itemID2) throws TasteException {
        double similarity = this.vectors.similarity(String.valueOf(itemID1), String.valueOf(itemID2));
        if (similarity != Double.NaN) {
            return similarity;
        }

        // TODO: 对model中没有的新闻，推测其向量，再计算相似度

        return 0;
    }

    @Override
    public double[] itemSimilarities(long itemID1, long[] itemID2s) throws TasteException {
        double[] result = new double[itemID2s.length];
        for (int i = 0; i < itemID2s.length; i++) {
            result[i] = itemSimilarity(itemID1, itemID2s[i]);
        }

        return result;
    }

    @Override
    public long[] allSimilarItemIDs(long itemID) throws TasteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(Collection<Refreshable> alreadyRefreshed) {

    }
}
