package com.kadoufall.recommender.service.contentBased;

import lombok.extern.log4j.Log4j;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.SimpleLabelAwareIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Log4j
public class TrainVsmModel {

    public static void main(String[] args) {
        try {
//             train();
            test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void train() throws Exception {
        List<LabelledDocument> labelledDocumentList = new ArrayList<>();
        List<String> stopwords = new ArrayList<>();

        FileInputStream fis = new FileInputStream("src/main/resources/caixinWords.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            String[] splitLine = line.trim().split("\t");
            String newsId = splitLine[0];
            String content = splitLine[1];

            LabelledDocument document = new LabelledDocument();
            document.addLabel(newsId);
            document.setContent(content);
            labelledDocumentList.add(document);
        }
        br.close();
        fis.close();


        fis = new FileInputStream("src/main/resources/stopword.txt");
        br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            stopwords.add(line.trim());
        }
        br.close();
        fis.close();

        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        LabelAwareIterator labelAwareIterator = new SimpleLabelAwareIterator(labelledDocumentList);
        ParagraphVectors paragraphVectors = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minWordFrequency(3)
                .minLearningRate(0.001)
                .batchSize(1000)
                .layerSize(200)
                .epochs(70)
                .stopWords(stopwords)
                .trainSequencesRepresentation(true)
                .trainElementsRepresentation(true)
                .windowSize(5)
                .iterate(labelAwareIterator)
                .labelsSource(labelAwareIterator.getLabelsSource())
                .tokenizerFactory(tokenizerFactory)
                .build();

        paragraphVectors.fit();

        WordVectorSerializer.writeParagraphVectors(paragraphVectors, "src/main/resources/model.pv");
    }

    private static void test() throws Exception {
        ClassPathResource resource = new ClassPathResource("/model.pv");
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        ParagraphVectors vectors = WordVectorSerializer.readParagraphVectors(resource.getFile());
        vectors.setTokenizerFactory(t);

        System.out.println(vectors.similarity("100649040", "100648768"));
        System.out.println(vectors.similarity("100649040", "100646087"));
        System.out.println(vectors.similarity("100649202", "100646120"));
        System.out.println(vectors.similarity("100649202", "1"));


        String str100649040 = "NASA 发布 疑似 马航 航班 失事 地点 高清 地图 美国宇航局 发布 图片 显示 北京 时间 月 日 11 35 NASA 卫星 Terra MODIS 拍摄 相关 海域 一张 250 米 精度 卫星 图像 图片 定位 约 104.6 E 6.1 N 红圈 疑似 马航 370 失事 地点 图片 来源 NASA 网站";
        String str100648768 = "越南 军机 发现 油迹 美国有线电视新闻网 报道 越南 一位 官员 告诉 记者 称 越南 飞机 越南 马来西亚 重叠 水域 表面 发现 疑似 油污 碎片 物体 另据 美联社 华尔街日报 报道 一架 参与 搜救 越南 飞机 马航 客机 疑似 失去 联系 越南 南部 海域 发现 两处 浮油 带 各方 消息 日 凌晨 马航 北京 第二场 发布会 中称 尚 得到 MH370 航班 去向 确实 消息 越南 方面 发现 疑似 飞机 失事 油迹 并未 确认 航班 CFP1";
        String str100646087 = "2014 两会 委员 吃 月 日 北京 政协 经济界 农业 界 委员 驻地 委员 吃 自助餐 用餐 简单 为主 餐品 往年 少 凉菜 三种 水果 两样 肉菜 鸡腿 扣肉 小炒 酒 现场 制作 担担面 糊塌子 江心 CFP1";
        String str100649202 = "官方 称 月 日 天安门 突发事件 已获 处置 人民 公安 报 消息 月 日 十二届 全国人大 二次 会议 开幕 10 时 45 分 北京市公安局 轨道 建设 分局 筹备组 支援 民警 张新 天安门 地区 分局 巡警 二 大队 副大队长 汪 湘江 成功 处置 一起 突发事件 当日 人获 市局 嘉奖 公安部 副 部长 北京 市委常委 公安局 局长 傅政华 随即 颁发 嘉奖 证书 激发 队伍 活力 做好 全国 两会 安保 工作 月 27 日 北京市公安局 启动 战时 党建 战时 表彰 战时 爱警 三 同步 战时 思想 政治 工作 机制 … … 北京市公安局 充分发挥 党建 工作 牵动 引领 组织 保障 作用 切实 党 政治 优势 组织 优势 转化 推动 安保 工作 强大 动力 圆满完成 各项 安保 任务 提供 坚强 组织 保障 党组织 安保 工作 动力源 不到 10 平方米 办公室 每隔 几分钟 一趟 地铁 列车 下面 隧道 驶过 地板 随之 微微 震动 月 日 北京市公安局 公交 总队 前门 站 派出所 前门 站 警务 站 记者 民警 交流 提高 音量 前门 站 地铁 通往 天安门广场 南大门 民警 常年 地铁站 台 守护 平安 充满 噪音 空气 混浊 地下 环境中工作 一天 地下 执勤 值班 10 余个 小时 不见 阳光 民警 保持 高昂 斗志 这是 精神 力量 前门 站 派出所 政委 赵鹏 说 每天 地铁 末班车 晚上 11 点半时 每个 党小组 利用 旅客 少 间隙 当天 勤务 进行 点评 民警 加油 鼓劲 支部 堡垒 小组 一块 阵地 党员 一面 旗帜 全国 两会 安保 期间 北京市 局对 单位 参加 安保 抽调 民警 属地 单位 民警 组成 集体 层层 组建 临时 党支部 党小组 强化 党建 核心作用 今年 市局 特别 设置 党建 微信群 民警 手机 收到 党建 信息 单位 党建 工作 实时 交流 即时 跟进 加强 基层单位 党建 工作 指导 党建 工作 落 每个 民警 身上 做到 岗位 全国 两会 期间 担负 天安门 周边 交通 安保 任务 帅府园 交警大队 每天 负责 勤务 100 民警 早 忙 晚 吃饭 只能 抓住 勤务 间隙 赶紧 扒 几口 月 日 记者 采访 民警 时 只能 见缝插针 地谈 几分钟 天安门 地区 分局 巡警 三 大队 大队长 王东明 休息 间隙 记者 采访 党建 促进 民警 充分发挥 作用 素质 体现 岗位 榜样 党员 民警 最能体现 带动 队伍 战斗力 55 岁 老 党员 天安门 地区 分局 巡警 三 大队 民警 陈晓鲁 膝盖 肿痛 坚守岗位 年轻 民警 父母 年纪 我要 年轻人 带个 好头 工作 做好 王博 高宇 一对 夫妻 北京市公安局 党校 副 科职 青干班 学员 全国 两会 期间 市局 干部 竞争 选拔 工作 要求 党校 安排 天安门 地区 分局 实战 锻炼 月 14 日 开展 巡控 工作 随同 分局 作战 单元 四班 三 运转 一连几天 见不着 面是 常事 自觉 克服困难 不让 分神 高宇 岗位 突发 阑尾炎 特意 同事 瞒 丈夫 不想 分心 影响 工作 一群 默默 岗位 坚守 守护 首都 平安 三个 领导 干部 做 表率 月 日 23 时 东城 分局 巡 特警 支队 民警 王魏峰 终于 提前 下班 班 市局 勤务 指挥部 党委书记 王赤 23 时至 次日 时 勤务 指挥部 32 名 党员 民警 分成 班次 辛苦 值班 一线 民警 替岗 执勤 寒夜 中为 一线 执勤 民警 一班 岗 执 一次 勤 基层 民警 得到 休息 调整 广大 民警 切身感受 市局 党委 关爱 温暖 王赤 说 全国 两会 安保 工作 期间 北京市公安局 组织 全局 两级 党委 第一 党支部 党员 开展 三个 活动 一次 政治动员 一次 替岗 执勤 一次 专题报告 激发 队伍 战斗力 领导 干部 关键 重大 安保 期间 市局 民警 进行 思想 发动 外 还会 安排 专人 带 发现 典型 事例 问题 苗头 双重 任务 采用 政工干部 驻 制 方式 单位 领导 干部 进行 跟进 考察 领导 干部 是否 状态 是否 一线 是否 尽责 今年 全国 两会 期间 市局 采用 24 小时 定时 检查 方式 白天 例行 检查 增加 夜里 巡查 晚上 11 点 出门 检查 多个 单位 往往 回来 凌晨 三四点 一位 考察组 成员 说 单位 一把手 一线 民警 并肩作战 多地 民警 多值 一天 勤 多替 一班 岗 轻伤 火线 丰台 分局 副局长 罗明 今年 全国 两会 前 不慎 摔伤 右手 骨折 面对 安保 任务 分局 领导 罗明 在家 养伤 打着 绷带 石膏 情况 坚守 安保 一线 组织 关爱 外 繁忙 勤务 中 战友 之间 搭 把手 帮个 忙 照应 北京市公安局 单位 发出 战友 替班 岗 倡议书 倡导 民警 分担 工作 树 榜样 激励 民警 争先 创优 全国 两会 安保 期间 市局 每晚 召开 领导 干部 工作 例会 总结 当日 情况 提示 次日 工作 表现 特别 突出 民警 集体 通报 表彰 月 日晚 工作 例会 市局 政治部 代表 党委 西城 分局 月坛 派出所 民警 李云成 通报 表扬 月 日 医院 下达 李云成 母亲 病危 通知 连续 工作 安保 一线 李云成 仅 请假 赶到 医院 母亲 一眼 返回 工作岗位 当天 晚上 10 点多 谢绝 领导 调岗 要求 辛苦 扛 扛 过去 月 日 凌晨 时许 正在 两会 住 巡逻 李云成 接到 爱人 医院 打来 电话 云成 妈 不行 战友 替班 李云成 飞奔 赶往 医院 最终 没能 见 老人 一眼 全国 两会 安保 工作 期间 月坛 派出所 负责 两个 住 安保 任务 两会 安保 任务 外 月坛 派出所 负责 辖区 服务 群众 打击 防范 犯罪 工作 派出所 政委 宋竣 患有 严重 腰椎 疾病 腰里 镶着 钢板 副所长 孟凡旺 患有 腰椎间盘 突出 症 发作 时 疼痛 难忍 袜子 穿 党员 民警 轻伤 火线 实际行动 感染 身边 民警 一个个 平凡 岗位 感人事迹 党建 及时 汇聚 市局 分局 党委 面前 表现 特别 突出 民警 党委 立即 启动 重大 活动 安保 重要 任务 工作 时期 表彰 机制 优秀 民警 获得 表彰 通过考察 机制 重点 考察 党员 发展 对象 及时 举措 鼓舞 民警 士气 激发 队伍 干劲 出现 民警 创先争优 局面 人民 公安 报 记者 丘勋锐 胡 爱华";
        String str100646120 = "天安门广场 安保 全面 升级 月 日 武警 驾驶 敞篷 警车 长安街 巡逻 东方 IC1";
        String str1 = "复旦 大学 计算机 MES APS";


        INDArray i1 = vectors.inferVector(str100649040);
        INDArray i2 = vectors.inferVector(str100648768);
        INDArray i3 = vectors.inferVector(str100646087);
        INDArray i4 = vectors.inferVector(str100649202);
        INDArray i5 = vectors.inferVector(str100646120);
        INDArray i6 = vectors.inferVector(str1);
        System.out.println(Transforms.cosineSim(i1, i2));
        System.out.println(Transforms.cosineSim(i1, i3));
        System.out.println(Transforms.cosineSim(i4, i5));
        System.out.println(Transforms.cosineSim(i4, i6));
    }


}
