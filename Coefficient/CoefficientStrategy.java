package com.github.ryan6073.Seriously.Coefficient;
import com.github.ryan6073.Seriously.BasicInfo.*;
import Jama.Matrix;

import java.util.*;

public class CoefficientStrategy {

    public Set<String> lastYearPapers;
    public Set<String> currentYearPapers;
    int currentYear = 0;
    //论文等级level 年龄状态time 状态转移矩阵
    Map<Integer,double[][][][]> transitionMatrixs = new HashMap<>();
    //估计值
    double[][][][] estimatedMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
    //误差值
    double[][][][] devMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
    //最终值
    double[][][][] resultMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];

    public CoefficientStrategy(){
        lastYearPapers = new HashSet<>();
        currentYearPapers = new HashSet<>();
        for(int i=0;i<LevelManager.Level.levelNum;i++)
            for(int j=0;j<LevelManager.PaperAgeGroup.ageGroupNum;j++) {
                for (int k = 0; k < LevelManager.CitationLevel.citationLevelNum; k++)
                    for (int m = 0; m < LevelManager.CitationLevel.citationLevelNum; m++) {
                        estimatedMatrix[i][j][k][m] = 0.0;
                        devMatrix[i][j][k][m] = 0.0;
                        resultMatrix[i][j][k][m] = 0.0;
                    }
                for(int k = 0; k < LevelManager.CitationLevel.citationLevelNum; k++){
                    estimatedMatrix[i][j][k][k] = 1.0;
                    resultMatrix[i][j][k][k] = 1.0;
                }
            }
    }

    //在第一次求出转移矩阵（即startyear-1）后，对于otherMatrix进行初始化设置
    public void initOtherMatrixs(){
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        //初始化前必须确保首个转移矩阵已经求出
        if(transitionMatrixs.containsKey(dataGatherManager.startYear-1)) {
            estimatedMatrix = transitionMatrixs.get(dataGatherManager.startYear - 1);
            //dev为全零矩阵，之前已经初始化 result无需初始化
        }else{
            System.out.println("Strategy矩阵初始化非法");
        }
    }

    //定义一个initTransitionMatrixItems方法，用于计算特定等级论文特定时间段的转移矩阵
    public void initorUpdateTransitionMatrixItems(int year){
        currentYear = year;
        if(transitionMatrixs.get(currentYear)==null) {
            double[][][][] transitionMatrix = new double[LevelManager.Level.levelNum][LevelManager.PaperAgeGroup.ageGroupNum][LevelManager.CitationLevel.citationLevelNum][LevelManager.CitationLevel.citationLevelNum];
            DataGatherManager dataGatherManager = DataGatherManager.getInstance();
            for(int i=0;i<LevelManager.Level.levelNum;i++)
                for(int j=0;j<LevelManager.PaperAgeGroup.ageGroupNum;j++)
                    for(int h=0;h<LevelManager.CitationLevel.citationLevelNum;h++)
                        for(int k=0;k<LevelManager.CitationLevel.citationLevelNum;k++)
                            transitionMatrix[i][j][h][k] = 0.0;

            for(String doi:lastYearPapers){
                Paper paper1 = dataGatherManager.dicDoiPaper.get(doi);
                double[][] childTransitionMatrix = transitionMatrix[paper1.getLevel().getIndex()][LevelManager.PaperAgeGroup.CHILD.getIndex()];
                double[][] youngTransitionMatrix = transitionMatrix[paper1.getLevel().getIndex()][LevelManager.PaperAgeGroup.YOUNG.getIndex()];
                double[][] oldTransitionMatrix = transitionMatrix[paper1.getLevel().getIndex()][LevelManager.PaperAgeGroup.OLD.getIndex()];
                childTransitionMatrix[paper1.matureCitationLevel.getIndex()][paper1.startCitationLevel.getIndex()]+=1.0;
                youngTransitionMatrix[paper1.matureCitationLevel.getIndex()][paper1.youthCitationLevel.getIndex()]+=1.0;
                oldTransitionMatrix[paper1.matureCitationLevel.getIndex()][paper1.strongCitationLevel.getIndex()]+=1.0;
            }
            //每列进行归一化
            for(int level=0;level<LevelManager.Level.levelNum;level++) {
                double[][] childTransitionMatrix = transitionMatrix[level][LevelManager.PaperAgeGroup.CHILD.getIndex()];
                double[][] youngTransitionMatrix = transitionMatrix[level][LevelManager.PaperAgeGroup.YOUNG.getIndex()];
                double[][] oldTransitionMatrix = transitionMatrix[level][LevelManager.PaperAgeGroup.OLD.getIndex()];
                double sum1 = 0.0;
                double sum2 = 0.0;
                double sum3 = 0.0;
                for (int i = 0; i < LevelManager.CitationLevel.citationLevelNum; i++) {
                    for (int j = 0; j < LevelManager.CitationLevel.citationLevelNum; j++) {
                        sum1 += childTransitionMatrix[j][i];
                        sum2 += youngTransitionMatrix[j][i];
                        sum3 += oldTransitionMatrix[j][i];
                    }
                    for (int j = 0; j < LevelManager.CitationLevel.citationLevelNum; j++) {
                        if (sum1 != 0.0)
                            childTransitionMatrix[j][i] /= sum1;
                        if (sum2 != 0.0)
                            youngTransitionMatrix[j][i] /= sum2;
                        if (sum3 != 0.0)
                            oldTransitionMatrix[j][i] /= sum3;
                    }
                }
            }
            transitionMatrixs.put(currentYear-1,transitionMatrix);
            lastYearPapers = currentYearPapers;
            currentYearPapers = new HashSet<>();
        }
    }

    //定义一个updateOtherTransitionMatrix方法，更新目标范围内的矩阵
    public void updateOtherTransitionMatrixs(){
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


        double[][][][] LastestTransitionMatrix = transitionMatrixs.get(currentYear-1);

        //遍历矩阵的每个元素
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < x; j++) {
                for (int k = 0; k < y; k++) {
                    for (int l = 0; l < z; l++) {
                        estimatedMatrix[i][j][k][l] = 0.875 * estimatedMatrix[i][j][k][l] + 0.125 * LastestTransitionMatrix[i][j][k][l];
                        devMatrix[i][j][k][l] = 0.75 * devMatrix[i][j][k][l] + 0.25 * Math.abs(estimatedMatrix[i][j][k][l] - LastestTransitionMatrix[i][j][k][l]);
                        resultMatrix[i][j][k][l] = estimatedMatrix[i][j][k][l] + 4 * devMatrix[i][j][k][l];
                    }
                }
            }
        }
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
        double[] initialState;
        if(getCitationLevel(paper)== LevelManager.CitationLevel.HIGH)
            initialState = new double[]{1.0,0.0,0.0};
        else if(getCitationLevel(paper)== LevelManager.CitationLevel.MEDIUM)
            initialState = new double[]{0.0,1.0,0.0};
        else if(getCitationLevel(paper)== LevelManager.CitationLevel.LOW)
            initialState = new double[]{0.0,0.0,1.0};
        else return null;//直接报错

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

    // 定义一个方法，根据论文的被引排名，划分论文的引用量区间
    // 定义一个方法，根据论文的被引排名，划分论文的引用量区间
    public static LevelManager.CitationLevel getCitationLevel(Paper paper){
        int citationRank = getCitationRank(paper);
        List<Paper> similarPapers = getSimilarPapers(paper);
        int totalPapers = similarPapers.size();

        // 计算排名比例
        double citationPercentage = (double) citationRank / totalPapers;

        LevelManager.CitationLevel citationLevel; // 定义一个变量，表示论文的引用量区间
        if(citationPercentage <= 0.1){
            // 论文的被引排名在前10%的属于高引用量区间，返回 LevelManager.CitationLevel.HIGH
            citationLevel = LevelManager.CitationLevel.HIGH;
        } else if(citationPercentage > 0.1 && citationPercentage <= 0.5){
            // 论文的被引排名在10%到50%之间的属于中引用量区间，返回 LevelManager.CitationLevel.MEDIUM
            citationLevel = LevelManager.CitationLevel.MEDIUM;
        } else {
            // 论文的被引排名在50%以下的属于低引用量区间，返回 LevelManager.CitationLevel.LOW
            citationLevel = LevelManager.CitationLevel.LOW;
        }
        return citationLevel; // 返回论文的引用量区间
    }

    //获取排名
    // 定义一个方法 getSimilarPapers，用来获取和0
    public static Vector<Paper> getSimilarPapers(Paper paper) {
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
    public static void sortPapersByCitation(Vector<Paper> papers) {
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

    static int getCitationRank(Paper paper) {
        // 获取和论文相同领域，相同年份，相同期刊的其他论文的列表
        List<Paper> similarPapers = getSimilarPapers(paper);
        // 对论文列表按照引用量从高到低进行排序
        sortPapersByCitation((Vector<Paper>) similarPapers);
        // 定义一个变量，表示论文的被引排名，初始值为 1
        int citationRank = 1;
        // 遍历论文列表，找到论文的位置
        for (Paper p : similarPapers) {
            // 如果论文的引用量大于或等于当前遍历的论文的引用量，说明论文的排名不变，继续遍历
            if (paper.getPaperImpact()>= p.getPaperImpact()) {
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
