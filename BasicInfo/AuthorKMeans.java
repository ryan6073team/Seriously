package com.github.ryan6073.Seriously.BasicInfo;

import java.util.*;

public class AuthorKMeans {
    public static void AuthorKMeans(DataGatherManager dataGatherManager) {
        //只对已经存在于图中的作者进行等级排序
        Vector<Author> authors = new Vector<>();
        for(Author author:dataGatherManager.dicOrcidAuthor.values())
            if(author.getIfExist()==1)
                authors.add(author);
        // 根据Impact进行排序
        Collections.sort(authors);

        Map<Double,Vector<Author>> dicImpAu = new HashMap<>();
        // 创建一维数据
        List<Double> data = new ArrayList<>();
        for (Author author : authors) {
            data.add(author.getAuthorImpact());
            if(dicImpAu.containsKey(author.getAuthorImpact())){
                dicImpAu.get(author.getAuthorImpact()).add(author);

            }
            else{
                dicImpAu.put(author.getAuthorImpact(), new Vector<>());
                dicImpAu.get(author.getAuthorImpact()).add(author);
            }
        }

        // 指定簇的数量 (K)
        int k = 5;

        // 创建簇中心
        List<Double> clusterCenters = initializeClusterCenters(data, k);

        // 分配数据点到最近的簇
        List<List<Double> > clusters = assignDataToClusters(data, clusterCenters);

        // 迭代次数
        int maxIterations = 100;////////////////////////////////////////////////////////////////////////!收敛问题

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // 分配数据点到最近的簇
            clusters = assignDataToClusters(data, clusterCenters);

            // 更新簇中心
            clusterCenters = calculateClusterCenters(clusters);
        }

        // 更新作者的等级（rank）
        int rank = 0;
        for (List<Double> cluster : clusters) {
            for (Double item : cluster) {
                if (dicImpAu.containsKey(item)) {
                    for (Author author : dicImpAu.get(item)) {
                        LevelManager.Level level = LevelManager.Level.getLevelByIndex(rank);
                        author.setLevel(level);
                    }
                }
            }
            rank++;
        }
    }

    // 初始化簇中心
    public static List<Double> initializeClusterCenters(List<Double> data, int k) {
        List<Double> clusterCenters = new ArrayList<>();
        int step = data.size() / k;
        for (int i = 0; i < k; i++) {
            clusterCenters.add(data.get(i * step));
        }
        return clusterCenters;
    }

    // 将数据点分配到最近的簇
    public static List<List<Double>> assignDataToClusters(List<Double> data, List<Double> clusterCenters) {
        List<List<Double>> clusters = new ArrayList<>();
        for (int i = 0; i < clusterCenters.size(); i++) {
            clusters.add(new ArrayList<>());
        }

        for (Double dataPoint : data) {
            int nearestCluster = findNearestCluster(dataPoint, clusterCenters);
            clusters.get(nearestCluster).add(dataPoint);
        }

        return clusters;
    }

    // 找到最近的簇中心
    public static int findNearestCluster(Double dataPoint, List<Double> clusterCenters) {
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
            for (Double dataPoint : cluster) {
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
