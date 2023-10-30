package com.github.ryan6073.Seriously.BasicInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KMeans {
    public static void kMeans(DataGatherManager dataGatherManager) {
        Collections.sort(dataGatherManager.journals);// 根据IF进行排序
        // 创建一维数据
        double[] data = new double[dataGatherManager.journals.size()];
        for(int i=0;i<dataGatherManager.journals.size();i++){
            data[i]=dataGatherManager.journals.get(i).getIF();
        }

        // 指定簇的数量 (K)
        int k = 4;

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

            // 打印每个簇的中心
//            System.out.println("Iteration " + iteration + ":");
//            for (int i = 0; i < k; i++) {
//                System.out.println("Cluster " + i + " Center: " + clusterCenters.get(i));
//            }
        }

        //更新rank
        int i=0;
        int rank=1;
        for(List<Double> cluster : clusters){
            for(double item:cluster){
                dataGatherManager.journals.get(i).setRank(rank);
//                System.out.println( dataGatherManager.journals.get(i).getRank());
//                System.out.println( dataGatherManager.journals.get(i).getIF());
                i++;
            }
            rank++;
        }
    }

    // 初始化簇中心
    public static List<Double> initializeClusterCenters(double[] data, int k) {
        List<Double> clusterCenters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusterCenters.add(data[data.length*(2*(i+1)-1)/8]);
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
