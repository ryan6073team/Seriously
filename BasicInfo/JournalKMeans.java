package com.github.ryan6073.Seriously.BasicInfo;

import java.util.*;

public class JournalKMeans {
    public static void JournalkMeans(DataGatherManager dataGatherManager) {
        //只对已经在图中有记载的期刊进行impact排序
        Vector<Journal> journals = new Vector<>();
        for(Journal journal: dataGatherManager.journals)
            if(journal.getIfExist()==1)
                journals.add(journal);
        Collections.sort(journals);
        // 创建一维数据
        double[] data = new double[journals.size()];
        for(int i=0;i<journals.size();i++){
            data[i]=journals.get(i).getJournalImpact();
        }
        Map<Double,Vector<Journal>> dicImpJn = new HashMap<>();
        // 创建一维数据
        for (Journal journal : journals) {
            if(dicImpJn.containsKey(journal.getJournalImpact())){
                dicImpJn.get(journal.getJournalImpact()).add(journal);

            }
            else{
                dicImpJn.put(journal.getJournalImpact(), new Vector<>());
                dicImpJn.get(journal.getJournalImpact()).add(journal);
            }
        }
        // 指定簇的数量 (K)
        int k = 5;

        // 创建簇中心
        List<Double> clusterCenters = initializeClusterCenters(data, k);

        // 分配数据点到最近的簇
        List<List<Double>> clusters = assignDataToClusters(data, clusterCenters);

        // 迭代次数
        int maxIterations = 100;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // 分配数据点到最近的簇
            clusters = assignDataToClusters(data, clusterCenters);

            // 更新簇中心
            clusterCenters = calculateClusterCenters(clusters);

        }

        //更新level
        int level = 0;
        for (List<Double> cluster : clusters) {
            for (double item : cluster) {
                if (dicImpJn.containsKey(item)) {
                    for (Journal journal : dicImpJn.get(item)) {
                        journal.setLevel(LevelManager.Level.getLevelByIndex(level));
                    }
                }
            }
            level++;
        }
        //更新期刊的论文等级
        for(Paper paper: dataGatherManager.papers){
            paper.setLevel(dataGatherManager.dicNameJournal.get(paper.journal).getLevel());
        }
    }

    // 初始化簇中心
    public static List<Double> initializeClusterCenters(double[] data, int k) {
        List<Double> clusterCenters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusterCenters.add(data[data.length*(2*(i+1)-1)/10]);
        }
        return clusterCenters;
    }

    // 将数据点分配到最近的簇
    public static List<List<Double>> assignDataToClusters(double[] data, List<Double> clusterCenters) {
        List<List<Double>> clusters = new ArrayList<>();
        for (int i = 0; i < clusterCenters.size(); i++) {
            clusters.add(new ArrayList<>());
        }

        for (double dataPoint : data) {
            int nearestCluster = findNearestCluster(dataPoint, clusterCenters);
            clusters.get(nearestCluster).add(dataPoint);
        }

        return clusters;
    }

    // 找到最近的簇中心
    public static int findNearestCluster(double dataPoint, List<Double> clusterCenters) {
        int nearestCluster = 0;
        double minDistance = Math.abs(dataPoint - clusterCenters.get(0));

        for (int i = 1; i < clusterCenters.size(); i++) {
            double distance = Math.abs(dataPoint - clusterCenters.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                nearestCluster = i;
            }
        }

        return nearestCluster;
    }

    // 计算每个簇的中心
    public static List<Double> calculateClusterCenters(List<List<Double>> clusters) {
        List<Double> newClusterCenters = new ArrayList<>();
        for (List<Double> cluster : clusters) {
            double sum = 0.0;
            for (double dataPoint : cluster) {
                sum += dataPoint;
            }
            if (!cluster.isEmpty()) {
                double center = sum / cluster.size();
                newClusterCenters.add(center);
            }
        }
        return newClusterCenters;
    }
}
