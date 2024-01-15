package com.github.ryan6073.Seriously.Coefficient;
import com.github.ryan6073.Seriously.BasicInfo.*;
import Jama.Matrix;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;

import java.util.*;

public class CoefficientStrategy {


    //论文等级level 年龄状态time 状态转移矩阵
    Map<Integer,double[][][][]> transitionMatrixs = new HashMap<>();
    //
    double[] impactCoefficient = new double[LevelManager.CitationLevel.citationLevelNum];
    //估计值
    double[][][][] estimatedMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
    //误差值
    double[][][][] devMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
    //最终值
    double[][][][] resultMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];

    public CoefficientStrategy(){
        initImpactCoefficients();
        initMatrixs();
    }

    //高引用量对应高值？？
    private void initImpactCoefficients() {
        impactCoefficient[0] = 1.0;
        impactCoefficient[1] = 0.8;
        impactCoefficient[2] = 0.5;
    }

    private void initMatrixs(){
        for(int i=0;i<LevelManager.Level.levelNum;i++)
            for(int j=0;j<LevelManager.PaperAgeGroup.ageGroupNum;j++)
                for(int k=0;k<LevelManager.CitationLevel.citationLevelNum;k++)
                    for(int m=0;m<LevelManager.CitationLevel.citationLevelNum;m++) {
                        estimatedMatrix[i][j][k][m] = 0.0;
                        devMatrix[i][j][k][m] = 0.0;
                        resultMatrix[i][j][k][m] = 0.0;
                    }
    }

    public static List<Map> getYearandTimeStateFromMonthNum(int monthnum){
        List<Map> ans = new ArrayList<>();
        ans.add(new HashMap<String,Integer>());
        ans.add(new HashMap<String, LevelManager.TimeState>());
        int year = monthnum/12;
        int month = monthnum%12;
        LevelManager.TimeState timeState;
        if(month>0&&month<5)
            timeState = LevelManager.TimeState.PRE;
        else if(month>=5&&month<=8)
            timeState = LevelManager.TimeState.MIDDLE;
        else if(month>=9&&month<=11){
            timeState = LevelManager.TimeState.LATE;
        }
        else{
            year--;//12 月
            timeState = LevelManager.TimeState.LATE;
        }
        ans.get(1).put("year",year);
        ans.get(2).put("timestate",timeState);
        return ans;
    }

    // getLastCurPapers，获得目标等级的目标时间的论文数据（cur和last）
    private Map<String,Paper> getTargetTimePapers(int year, LevelManager.TimeState timeState, LevelManager.Level level){
        Map<String,Paper> cur = new HashMap<>();
        Vector<String> tempCur = new Vector<>();

        Set<TimeInfo> timeInfos = DataGatherManager.getInstance().dicTimeInfoDoi.keySet();
        switch (timeState) {
            case PRE -> {
                for (TimeInfo timeInfo : timeInfos) {
                    if (timeInfo.year == year) {
                        if (timeInfo.month >= 1 && timeInfo.month <= 4)
                            tempCur.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                    }
                }
            }
            case MIDDLE -> {
                for (TimeInfo timeInfo : timeInfos) {
                    if (timeInfo.year == year) {
                        if (timeInfo.month >= 5 && timeInfo.month <= 8)
                            tempCur.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                    }
                }
            }
            case LATE -> {
                for (TimeInfo timeInfo : timeInfos) {
                    if (timeInfo.year == year) {
                        if (timeInfo.month >= 9 && timeInfo.month <= 12)
                            tempCur.addAll(DataGatherManager.getInstance().dicTimeInfoDoi.get(timeInfo));
                    }
                }
            }
        }
        for(String doi:tempCur){
            Paper paper = DataGatherManager.getInstance().dicDoiPaper.get(doi);
            if(paper.getLevel()==level)
                cur.put(doi,paper);
        }

        return  cur;
    }

    // 定义一个loadPapersAgeGroup方法，用于将论文按照年龄组进行分类，并返回相应映射
    private Map<LevelManager.PaperAgeGroup, Vector<Paper>> loadPapersAgeGroup(Map<String,Paper> papers){
        // 创建一个Map对象，用于存储不同年龄组的论文列表
        Map<LevelManager.PaperAgeGroup, Vector<Paper>> map = new HashMap<>();
        for(int i=0;i<LevelManager.PaperAgeGroup.ageGroupNum;i++)
            map.put(LevelManager.PaperAgeGroup.getPaperAgeGroupByIndex(i),new Vector<Paper>());
        // 遍历每个年龄组的枚举值
        for(Map.Entry<String,Paper> paper:papers.entrySet()){
            LevelManager.PaperAgeGroup paperAgeGroup = paper.getValue().getAgeGroup();
            map.get(paperAgeGroup).add(paper.getValue());
        }
        return map;
    }

    // 定义一个calTransitionMatrixItem方法，用于将某个等级的论文的某个时间段的引用量转移矩阵算出，前提条件是需要相关时间段的论文数据
    private double[][] getTransitionMatrixItem(Map<String,Paper> currentPapers){
        double[][] ans = new double[LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
        for(int i=0;i<LevelManager.CitationLevel.citationLevelNum;i++)
            for(int j=0;j<LevelManager.CitationLevel.citationLevelNum;j++)
                ans[i][j] = 0.0;
        for(Paper paper:currentPapers){
            LevelManager.CitationLevel citationLevel = paper.getCitationLevel();
            //ans[target][source]
            ans[currentPapers.get(paper.getDoi()).getCitationLevel().getIndex()][citationLevel.getIndex()] += 1.0;
            //从paper的citationLevel转到temp的getCitationLevel的频数+1
        }
        //每列进行归一化
        for(int i=0;i<LevelManager.CitationLevel.citationLevelNum;i++) {
            double sum=0.0;
            for (int j = 0; j < LevelManager.CitationLevel.citationLevelNum; j++) {
                sum+=ans[j][i];
            }
            if(sum!=0.0)
                for (int j = 0; j < LevelManager.CitationLevel.citationLevelNum; j++) {
                    ans[j][i]/=sum;
                }
        }
        return ans;
    }

    //定义一个initTransitionMatrixItems方法，用于计算特定等级论文特定时间段的转移矩阵
    public void initorUpdateTransitionMatrixItems(int year, LevelManager.TimeState timeState/*获取基于某年某时间段的事件状态*/){

        if(transitionMatrixs.get(year)==null||transitionMatrixs.get(year).get(timeState)==null) {
            double[][][][] transitionMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
            //获取两组数据 数据Last代表year.timeState-1 数据Cur代表year.timeState
            //如year=2021 timeState=Pre 则数据Last代表2020年9-12月的论文数据集 数据Cur代表2021年1-4月的论文数据集 按照论文等级进行过滤
            for (int i = 0; i < LevelManager.Level.levelNum; i++) {
                Map<String,Paper> currentPapers = getTargetTimePapers(year, timeState, LevelManager.Level.getLevelByIndex(i));

                //将单个时间段的论文按照论文所处年龄段进行划分 青年 壮年 老年 成熟,显然last的老年会在cur中成熟 壮年会变为老年 以此类推
                Map<LevelManager.PaperAgeGroup, Vector<Paper>> dicAgeLastPaper = loadPapersAgeGroup(currentPapers);

                //获取在目标时间段下的各年龄状态的论文的状态转移矩阵
                //若该时间点已经没有目标年龄的论文，则返回的状态转移矩阵会是单位矩阵，即此类论文的状态不会再转移
                double[][] childTransitionMatrix = getTransitionMatrixItem(dicAgeLastPaper.get(LevelManager.PaperAgeGroup.CHILD));
                double[][] youngTransitionMatrix = getTransitionMatrixItem(dicAgeLastPaper.get(LevelManager.PaperAgeGroup.YOUNG));
                double[][] oldTransitionMatrix = getTransitionMatrixItem(dicAgeLastPaper.get(LevelManager.PaperAgeGroup.OLD));

                transitionMatrix[i][LevelManager.PaperAgeGroup.CHILD.getIndex()] = childTransitionMatrix;
                transitionMatrix[i][LevelManager.PaperAgeGroup.YOUNG.getIndex()] = youngTransitionMatrix;
                transitionMatrix[i][LevelManager.PaperAgeGroup.OLD.getIndex()] = oldTransitionMatrix;
            }
            // 将计算得到的转换矩阵存入transitionMatrixs中
            if (transitionMatrixs.get(year) == null) {
                transitionMatrixs.put(year, new HashMap<>());
            }
            transitionMatrixs.get(year).put(timeState, transitionMatrix);
        }

    }

    //定义一个updateOtherTransitionMatrix方法，更新目标范围内的矩阵
    public void updateOtherTransitionMatrixs(int year, LevelManager.TimeState timeState){
        //estimatedMatrix = 0.875*estimatedMatrix+0.125*TimeStateTransitionMatrix
        //计算DevTransitionMatrix
        //devMatrix = 0.75*devMatrix + 0.25*abs(estimatedMatrix-TimeStateTransitionMatrix)
        //得到最终值
        //resultMatrix = estimatedMatrix + 4*devMatrix

        //获取矩阵的维度
        int w = estimatedMatrix.length;
        int x = estimatedMatrix[0].length;
        int y = estimatedMatrix[0][0].length;
        int z = estimatedMatrix[0][0][0].length;


        double[][][][] TimeStateTransitionMatrix = transitionMatrixs.get(year).get(timeState);

        //遍历矩阵的每个元素
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < x; j++) {
                for (int k = 0; k < y; k++) {
                    for (int l = 0; l < z; l++) {

                        estimatedMatrix[i][j][k][l] = 0.875 * estimatedMatrix[i][j][k][l] + 0.125 * TimeStateTransitionMatrix[i][j][k][l];

                        devMatrix[i][j][k][l] = 0.75 * devMatrix[i][j][k][l] + 0.25 * Math.abs(estimatedMatrix[i][j][k][l] - TimeStateTransitionMatrix[i][j][k][l]);

                        resultMatrix[i][j][k][l] = estimatedMatrix[i][j][k][l] + 4 * devMatrix[i][j][k][l];
                    }
                }
            }
        }
    }

    // 定义一个getTransitionMatrix方法，根据论文的等级和年龄获取给定时间段的状态转移矩阵
    private double[][] getTransitionMatrix(int year, LevelManager.TimeState timeState, Paper paper){
        LevelManager.Level paperLevel = paper.getLevel(); // 获取论文的等级，返回0，1，2，3分别表示A，B，C，D
        LevelManager.PaperAgeGroup paperAgeGroup= paper.getAgeGroup();
        double[][] paperTransitionMatrix = transitionMatrixs.get(year).get(timeState)[paperLevel.getIndex()][paperAgeGroup.getIndex()]; // 获取论文的状态转移概率矩阵
        return paperTransitionMatrix;
    }
    // 定义一个getEstimatedTransitionMatrix方法，根据论文的等级和年龄获取根据时间累计的状态转移矩阵
    public double[][] getResultTransitionMatrix( Paper paper){
        LevelManager.Level paperLevel = paper.getLevel(); // 获取论文的等级，返回0，1，2，3分别表示A，B，C，D
        LevelManager.PaperAgeGroup paperAgeGroup= paper.getAgeGroup();
        double[][] paperTransitionMatrix = resultMatrix[paperLevel.getIndex()][paperAgeGroup.getIndex()]; // 获取论文的状态转移概率矩阵
        return paperTransitionMatrix;
    }
    //只能用于不成熟论文，获取论文目前的状态矩阵
    public double[] getStateDistribution(Paper paper){
        double[] initialState = getInitialState(paper);
        double[][] columnVectorData = new double[initialState.length][1];
        for (int i = 0; i < initialState.length; i++) {
            columnVectorData[i][0] = initialState[i];
        }
        Matrix stateDistribution = new Matrix(columnVectorData);
        // 根据论文的年龄组和时间状态，获取对应的状态转移概率矩阵
        Matrix transitionMatrix = Matrix.constructWithCopy(getResultTransitionMatrix(paper));
        stateDistribution = transitionMatrix.times(stateDistribution); //传入论文在前一个时间点的状态分布和论文的状态转移概率矩阵，返回论文在当前时间点的状态分布
        for(int i=0;i<initialState.length;i++)
            initialState[i] = stateDistribution.getArray()[i][0];
        return initialState; // 返回stateDistribution的二维数组表示
    }

    //定义getInitialState方法，获取论文的初始状态矩阵，实际上为论文的初始引用区间
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
    private Vector<Paper> getSimilarPapers(Paper paper) {
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
    private void sortPapersByCitation(Vector<Paper> papers) {
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
