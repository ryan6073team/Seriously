package com.github.ryan6073.Seriously.Coefficient;
import com.github.ryan6073.Seriously.BasicInfo.*;
import Jama.Matrix;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class CoefficientStrategy {
    //高引用量对应高值？？
    public static double[] initImpactCoefficients() {
        double[] impactCoefficient = {1, 0.8, 0.5};
        return impactCoefficient;
    }
    //论文等级level 年龄状态time 状态转移矩阵
    double[][][][] transitionMatrix;

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

    Map<LevelManager.PaperAgeGroup, Vector<Paper>> loadPapersAgeGroup(Vector<Paper> papers){

        return null;
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
            double[][] childTransitionMatrix = loadTransitionMatrix(dicAgeLastPaper.get(LevelManager.PaperAgeGroup.CHILD), dicAgeCurPaper.get(LevelManager.PaperAgeGroup.YOUNG));
            double[][] youngTransitionMatrix = loadTransitionMatrix(dicAgeLastPaper.get(LevelManager.PaperAgeGroup.YOUNG), dicAgeCurPaper.get(LevelManager.PaperAgeGroup.OLD));
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

    public double[][] getStateDistribution(Paper paper){
        double[] initialState = getInitialState(paper);
        int citationLevel = getCitationLevel(paper); // 获取论文的引用量区间，返回0，1，2分别表示高，中，低
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        LevelManager.Level paperLevel = paper.getLevel();
        int timeLength = 12;// 定义论文的保护期长度
        Matrix[] stateDistribution = new Matrix[timeLength]; // 定义一个Matrix数组，表示论文在不同时间点的状态分布
        stateDistribution[0] = new Matrix(initialState, 1); // 将论文的初始状态分布转换为一个1行的Matrix对象，赋值给第一个时间点的状态分布
        // 用一个循环，根据论文的状态转移概率矩阵，计算论文在后续时间点的状态分布
        for(int i = 1; i < timeLength; i++){
            // 用times方法，计算论文在当前时间点的状态分布
            stateDistribution[i] = stateDistribution[i-1].times(Matrix.constructWithCopy(transitionMatrix[citationLevel][paperLevel.getIndex()])); //传入论文在前一个时间点的状态分布和论文的状态转移概率矩阵，返回论文在当前时间点的状态分布
        }
        double[][] stateDistributionArray = new double[timeLength][3]; // 定义一个二维数组，用来存储stateDistribution的二维数组表示
        // 用一个循环，把stateDistribution的每个元素都转换成二维数组，并赋值给stateDistributionArray
        for(int i = 0; i < timeLength; i++){
            stateDistributionArray[i] = stateDistribution[i].getArray()[0]; // 用getArray方法获取stateDistribution[i]的二维数组表示，然后取第0行，赋值给stateDistributionArray[i]
        }
        return stateDistributionArray; // 返回stateDistribution的二维数组表示
    }

    public double[] matrixMultiply(double[] vector, double[][] matrix) {
        int m = vector.length;
        int n = matrix[0].length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                result[i] += vector[j] * matrix[j][i];
            }
        }
        return result;
    }
    private double[] getInitialState(Paper paper) {// 获取论文的初始状态分布，返回一个一维数组，表示论文在高，中，低影响力状态的概率
        //这里我在datamanager里面新增了一个叫做dicDoiStateCoefficient的成员变量，它是从doi到论文当前状态数组的映射，所以前三个字母是dic(tionary)
        //datamanager里面的其它变量也是这样命名的
        //所以我增加了一个paper参数在函数里
        return DataGatherManager.getInstance().dicDoiStateCoefficient.get(paper.getDoi());
    }
    // 定义一个方法，根据论文的状态分布，计算论文的影响力系数的期望值
    public double getPaperImpactCoefficientExpectation(double[] stateDistribution){
        double paperImpactCoefficientExpectation = 0; // 定义一个变量，表示论文的影响力系数的期望值
        // 用一个循环，计算论文的影响力系数的期望值
        double[] impactCoefficient=initImpactCoefficients();
        for(int i = 0; i < 3; i++){
            // 论文的影响力系数的期望值等于它的状态分布和它的状态影响力系数的加权平均值
            paperImpactCoefficientExpectation += stateDistribution[i] * impactCoefficient[i]; // 将论文在当前状态的概率乘以论文在当前状态的影响力系数，累加到论文的影响力系数的期望值上
        }
        return paperImpactCoefficientExpectation; // 返回论文的影响力系数的期望值
    }

    // 定义一个方法，根据论文的被引排名，划分论文的引用量区间
    public int getCitationLevel(Paper paper){
        // 假设论文的引用量区间是根据论文在同一领域，同一年份，同一期刊的被引排名来划分的
        // 假设论文的引用量区间有三个，分别是高，中，低
        // 假设论文的被引排名在前10%的属于高引用量区间，返回0
        // 假设论文的被引排名在10%到50%之间的属于中引用量区间，返回1
        // 假设论文的被引排名在50%以下的属于低引用量区间，返回2
        int citationRank = getCitationRank(paper);
        int citationLevel = 0; // 定义一个变量，表示论文的引用量区间
        if(citationRank <= 0.1){
            // 论文的被引排名在前10%的属于高引用量区间，返回0
            citationLevel = 0;
        }else if(citationRank > 0.1 && citationRank <= 0.5){
            // 论文的被引排名在10%到50%之间的属于中引用量区间，返回1
            citationLevel = 1;
        }else{
            // 论文的被引排名在50%以下的属于低引用量区间，返回2
            citationLevel = 2;
        }
        return citationLevel; // 返回论文的引用量区间
    }
    private int getCitationRank(Paper paper) {// 获取论文的被引排名，返回一个整数，表示论文在同一领域，同一年份，同一期刊的被引排名
        return 0;
    }

}

/*
1.把impactCoefficient给初始化了
2.新增citationLevel的相关类
3.将代码与jama适配
 */