# 基于Mahout的新闻推荐系统

## 相关技术

- 推荐算法
    - 基于用户的协同过滤
    - 基于内容的推荐
    - 基于热点的推荐

- [Mahout](http://mahout.apache.org/)：整体框架，实现了协同过滤
- [Deeplearning4j](https://deeplearning4j.org/)，构建VSM
- [Jieba](https://github.com/fxsjy/jieba)：分词，关键词提取
- [HanLP](https://github.com/hankcs/HanLP)：分词，关键词提取
- [Spring Boot](https://spring.io/)：提供API、ORM


## 关键实现

### 基于用户的协同过滤
- 直接调用Mahout相关接口即可
- 选择不同的用户相似度度量方法，这里选择了基于谷本系数、基于对数似然和基于曼哈顿距离

### 基于内容的推荐
- 对新闻文本进行分词
- 调用Deeplearning4j中构建paragraphvector的方法，通过doc2vec构建VSM
- 用Gensim会更方便点


### 基于热点的推荐
- 统计最高浏览量
- 过滤一定时间前的新闻保证热点的准确

## 评测指标
- [测试数据集](https://pan.baidu.com/s/1Y84iLIY8RbO_6oFTEm1oGA#list/path=%2F)
- F1-Measure(precision + recall)

|算法|最近邻数量K|推荐数N|F1-Measure|
|:---:|:---:|:---:|:---:|
|UserCF--Tanimoto|20|11|0.481591183699049|
|UserCF--LogLike|10|11|0.486337485027766|
|UserCF--CityBlock|30|8|0.424612102745937|
|ContentBased|-|5|0.0491655390166893|
|HotSpots|-|14|0.118524972063865|


