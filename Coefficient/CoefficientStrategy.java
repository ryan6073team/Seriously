package com.github.ryan6073.Seriously.Coefficient;
import com.github.ryan6073.Seriously.BasicInfo.*;
import Jama.Matrix;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;

import java.util.*;

public class CoefficientStrategy {
    //高引用量对应高值？？
    public static double[] initImpactCoefficients() {
        double[] impactCoefficient = {1, 0.8, 0.5};
        return impactCoefficient;
    }
    //论文等级level 年龄状态time 状态转移矩阵
    static double[][][][] transitionMatrix;

    Vector<Paper>[] getTargetDate(int year, LevelManager.TimeState timeState, LevelManager.Level level){
        Vector<Paper> cur = new Vector<>();
        Vector<Paper> last = new Vector<>();

        Vector<String> tempCur = new Vector<>();
        Vector<String> tempLast = new Vector<>();

        Set<TimeInfo> timeInfos = DataGatherManager.getInstance().dicTimeInfoDoi.keySet();
        switch (timeState) {
            case PRE:
                for (TimeInfo timeInfo : timeInfos) {
                    if (timeInfo.year == year) {
                        if (timeInfo.month >= 1 && timeInfo.month <= 4)
                            tempCur.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                    } else if (timeInfo.year == year - 1) {
                        if (timeInfo.month >= 9 && timeInfo.month <= 12)
                            tempLast.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                    }
                }
            case MIDDLE:
                for (TimeInfo timeInfo : timeInfos) {
                    if (timeInfo.year == year) {
                        if (timeInfo.month >= 5 && timeInfo.month <= 8)
                            tempCur.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                        else if (timeInfo.month >= 1 && timeInfo.month <= 4)
                            tempLast.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                    }
                }
            case LATE:
                for (TimeInfo timeInfo : timeInfos) {
                    if (timeInfo.year == year) {
                        if (timeInfo.month >= 9 && timeInfo.month <= 12)
                            tempCur.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                        else if (timeInfo.month >= 5 && timeInfo.month <= 8)
                            tempLast.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                    }
                }
        }
        for(String doi:tempCur){
            Paper paper = DataGatherManager.getInstance().dicDoiPaper.get(doi);
            if(paper.getLevel()==level)
                cur.add(paper);
        }
        for(String doi:tempLast){
            Paper paper = DataGatherManager.getInstance().dicDoiPaper.get(doi);
            if(paper.getLevel()==level)
                last.add(paper);
        }
        Vector<Paper>[] ans = new Vector[2];
        ans[0] = last;
        ans[1] = cur;
        return ans;
    }

    // 定义一个loadPapersAgeGroup方法，用于将论文按照年龄组进行分类
    public Map<LevelManager.PaperAgeGroup, Vector<Paper>> loadPapersAgeGroup(Vector<Paper> papers){
        // 创建一个Map对象，用于存储不同年龄组的论文列表
        Map<LevelManager.PaperAgeGroup, Vector<Paper>> map = new HashMap<>();
        // 遍历每个年龄组的枚举值
        for(LevelManager.PaperAgeGroup ageGroup : LevelManager.PaperAgeGroup.values()){
            Vector<Paper> vector = new Vector<>();
            for(Paper paper : papers){
                LevelManager.PaperAgeGroup paperAgeGroup = paper.getAgeGroup();
                if(paperAgeGroup == ageGroup){
                    vector.add(paper);
                }
            }
            map.put(ageGroup, vector);
        }
        return map;
    }


    double[][] loadTransitionMatrix(Vector<Paper> pastPapers, Vector<Paper> currentPapers){
        double[][] ans = new double[LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
        for(int i=0;i<LevelManager.CitationLevel.citationLevelNum;i++)
            for(int j=0;j<LevelManager.CitationLevel.citationLevelNum;j++)
                ans[i][j] = 0.0;
        for(Paper paper:pastPapers){
            LevelManager.CitationLevel citationLevel = paper.getCitationLevel();
            for(Paper temp:currentPapers){
                if(temp.getDoi() == paper.getDoi()) {
                    ans[temp.getCitationLevel().getIndex()][citationLevel.getIndex()] += 1.0;
                    //从paper的citationLevel转到temp的getCitationLevel的频数+1
                    break;
                }
            }
        }
        for(int i=0;i<LevelManager.CitationLevel.citationLevelNum;i++)
            for(int j=0;j<LevelManager.CitationLevel.citationLevelNum;j++)
                ans[i][j] = ans[i][j]/pastPapers.size();

        return ans;
    }

    public double[][][][] initTransitionMatrix(int year, LevelManager.TimeState timeState/*获取基于某年某时间段的事件状态*/){
        transitionMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
        //获取两组数据 数据Last代表year.timeState-1 数据Cur代表year.timeState
        //如year=2021 timeState=Pre 则数据Last代表2020年9-12月的论文数据集 数据Cur代表2021年1-4月的论文数据集 按照论文等级进行过滤
        for(int i=0;i<LevelManager.Level.levelNum;i++) {
            Vector<Paper>[] Papers = getTargetDate(year, timeState, LevelManager.Level.getLevelByIndex(i));
            Vector<Paper> Last = Papers[0];
            Vector<Paper> Cur = Papers[1];

            //将单个时间段的论文按照论文所处年龄段进行划分 青年 壮年 老年 成熟,显然last的老年会在cur中成熟 壮年会变为老年 以此类推
            Map<LevelManager.PaperAgeGroup, Vector<Paper>> dicAgeLastPaper = loadPapersAgeGroup(Last);
            Map<LevelManager.PaperAgeGroup, Vector<Paper>> dicAgeCurPaper = loadPapersAgeGroup(Cur);

            //获取在目标时间段下的各年龄状态的论文的状态转移矩阵
            double[][] childTransitionMatrix = loadTransitionMatrix(dicAgeLastPaper.get(LevelManager.PaperAgeGroup.CHILD), dicAgeCurPaper.get(LevelManager.PaperAgeGroup.MATURE));
            double[][] youngTransitionMatrix = loadTransitionMatrix(dicAgeLastPaper.get(LevelManager.PaperAgeGroup.YOUNG), dicAgeCurPaper.get(LevelManager.PaperAgeGroup.MATURE));
            double[][] oldTransitionMatrix = loadTransitionMatrix(dicAgeLastPaper.get(LevelManager.PaperAgeGroup.OLD), dicAgeCurPaper.get(LevelManager.PaperAgeGroup.MATURE));

            transitionMatrix[i][LevelManager.PaperAgeGroup.CHILD.getIndex()] = childTransitionMatrix;
            transitionMatrix[i][LevelManager.PaperAgeGroup.YOUNG.getIndex()] = youngTransitionMatrix;
            transitionMatrix[i][LevelManager.PaperAgeGroup.OLD.getIndex()] = oldTransitionMatrix;
        }
        return transitionMatrix;
    }

    // 定义一个方法，根据论文的等级和年龄获取状态转移矩阵
    public double[][] getTransitionMatrix(Paper paper){
        LevelManager.Level paperLevel = paper.getLevel(); // 获取论文的等级，返回0，1，2，3分别表示A，B，C，D
        LevelManager.PaperAgeGroup paperAgeGroup= paper.getAgeGroup();
        double[][] paperTransitionMatrix = transitionMatrix[paperLevel.getIndex()][paperAgeGroup.getIndex()]; // 获取论文的状态转移概率矩阵
        return paperTransitionMatrix;
    }
    //只能用于不成熟论文
    public double[] getStateDistribution(Paper paper){
        double[] initialState = getInitialState(paper);
        double[][] columnVectorData = new double[initialState.length][1];
        for (int i = 0; i < initialState.length; i++) {
            columnVectorData[i][0] = initialState[i];
        }
        Matrix stateDistribution = new Matrix(columnVectorData);
        // 根据论文的年龄组和时间状态，获取对应的状态转移概率矩阵
        Matrix transitionMatrix = Matrix.constructWithCopy(getTransitionMatrix(paper));
        stateDistribution = transitionMatrix.times(stateDistribution); //传入论文在前一个时间点的状态分布和论文的状态转移概率矩阵，返回论文在当前时间点的状态分布
        for(int i=0;i<initialState.length;i++)
            initialState[i] = columnVectorData[i][0];
        return initialState; // 返回stateDistribution的二维数组表示
    }



    private double[] getInitialState(Paper paper) {
        if(getCitationLevel(paper)== LevelManager.CitationLevel.HIGH)
            return new double[]{1.0,0.0,0.0};
        else if(getCitationLevel(paper)== LevelManager.CitationLevel.MEDIUM)
            return new double[]{0.0,1.0,0.0};
        else if(getCitationLevel(paper)== LevelManager.CitationLevel.LOW)
            return new double[]{0.0,0.0,1.0};
        else return null;
    }

    // 定义一个方法，根据论文的状态分布，计算论文的影响力系数的期望值
    public double getPaperImpactCoefficientExpectation(double[] stateDistribution){
        double paperImpactCoefficientExpectation = 0; // 定义一个变量，表示论文的影响力系数的期望值
        double[] impactCoefficient=initImpactCoefficients();
        for(int i = 0; i < 3; i++){
            // 论文的影响力系数的期望值等于它的状态分布和它的状态影响力系数的加权平均值
            paperImpactCoefficientExpectation += stateDistribution[i] * impactCoefficient[i]; // 将论文在当前状态的概率乘以论文在当前状态的影响力系数，累加到论文的影响力系数的期望值上
        }
        return paperImpactCoefficientExpectation; // 返回论文的影响力系数的期望值
    }

    // 定义一个方法，根据论文的被引排名，划分论文的引用量区间
    public LevelManager.CitationLevel getCitationLevel(Paper paper){
        int citationRank = getCitationRank(paper);
        LevelManager.CitationLevel citationLevel; // 定义一个变量，表示论文的引用量区间
        if(citationRank <= 0.1){
            // 论文的被引排名在前10%的属于高引用量区间，返回 LevelManager.CitationLevel.HIGH
            citationLevel = LevelManager.CitationLevel.HIGH;
        }else if(citationRank > 0.1 && citationRank <= 0.5){
            // 论文的被引排名在10%到50%之间的属于中引用量区间，返回 LevelManager.CitationLevel.MEDIUM
            citationLevel = LevelManager.CitationLevel.MEDIUM;
        }else{
            // 论文的被引排名在50%以下的属于低引用量区间，返回 LevelManager.CitationLevel.LOW
            citationLevel = LevelManager.CitationLevel.LOW;
        }
        return citationLevel; // 返回论文的引用量区间
    }
    //获取排名
// 定义一个方法 getSimilarPapers，用来获取和一个论文相同年份，相同期刊的其他论文的列表
    public Vector<Paper> getSimilarPapers(Paper paper) {
        // 创建一个空的向量，用来存储符合条件的论文
        Vector<Paper> similarPapers = new Vector<>();
        // 获取论文的年份，期刊
        int year = paper.getPublishedYear();
        String journal = paper.getJournal();
        // 遍历 DataGatherManager 中的所有论文
        for (Paper p : DataGatherManager.getInstance().papers) {
            // 如果论文的领域，年份，期刊和给定的论文相同，且不是给定的论文本身，那么将论文添加到向量中
            if ( p.getPublishedYear() == year && p.getJournal().equals(journal) && !p.equals(paper)) {
                similarPapers.add(p);
            }
        }
        // 返回向量
        return similarPapers;
    }

    // 定义一个方法 sortPapersByCitation，用来对一个论文向量按照引用量从高到低进行排序
    public void sortPapersByCitation(Vector<Paper> papers) {
        // 使用 Collections 类的 sort 方法，传入一个自定义的比较器，按照引用量从高到低进行排序
        Collections.sort(papers, new Comparator<Paper>() {
            // 重写 compare 方法，比较两个论文的引用量
            public int compare(Paper p1, Paper p2) {
                // 如果 p1 的引用量大于 p2 的引用量，返回 -1，表示 p1 排在 p2 前面
                if (p1.getCitedList().size() > p2.getCitedList().size()) {
                    return -1;
                    // 如果 p1 的引用量小于 p2 的引用量，返回 1，表示 p1 排在 p2 后面
                } else if (p1.getCitedList().size() < p2.getCitedList().size()) {
                    return 1;
                    // 如果 p1 的引用量等于 p2 的引用量，返回 0，表示 p1 和 p2 的顺序不变
                } else {
                    return 0;
                }
            }
        });
    }
    private int getCitationRank(Paper paper) {
        // 获取和论文相同领域，相同年份，相同期刊的其他论文的列表
        List<Paper> similarPapers = getSimilarPapers(paper);
        // 对论文列表按照引用量从高到低进行排序
        sortPapersByCitation((Vector<Paper>) similarPapers);
        // 定义一个变量，表示论文的被引排名，初始值为 1
        int citationRank = 1;
        // 遍历论文列表，找到论文的位置
        for (Paper p : similarPapers) {
            // 如果论文的引用量大于或等于当前遍历的论文的引用量，说明论文的排名不变，继续遍历
            if (paper.getCitedList().size() >= p.getCitedList().size()) {
                continue;
            } else {
                // 否则，说明论文的排名下降了，排名加一，继续遍历
                citationRank++;
            }
        }
        // 返回论文的被引排名
        return citationRank;
    }

}
