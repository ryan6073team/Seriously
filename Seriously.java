package com.github.ryan6073.Seriously;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Graph.GraphStore;
import com.github.ryan6073.Seriously.Impact.CalImpact;

public class Seriously {

    public static void main(String[] args) {
//        GraphStore.createTestGraph();
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
//      initJournalToIF(dataGatherManager);//更新期刊IF的映射

        FileInput.init(dataGatherManager);
        System.out.println("完成文件初始化");

        //新增的初始化datagather的startyear/month finalyear/month
        dataGatherManager.initYearMonth();
        System.out.println("完成时间初始化");

        //更新等级
        AuthorKMeans.AuthorKMeans(dataGatherManager);
        System.out.println("完成作者等级更新");

        JournalKMeans.JournalkMeans(dataGatherManager);
        System.out.println("完成期刊等级更新");

        GraphManager graphManager = GraphManager.getInstance();

        GraphInit.initGraph(graphManager,dataGatherManager,dataGatherManager.startYear,dataGatherManager.startMonth);
        System.out.println("完成初始总图的初始化");

        GraphStore.store("0000-0", graphManager.Graph);
        System.out.println("完成初始总图的存储");

        GraphInit.initGraphItems(graphManager,dataGatherManager,dataGatherManager.startYear,dataGatherManager.startMonth,dataGatherManager.finalYear,dataGatherManager.finalMonth);
        System.out.println("完成初始图集的初始化");
//        try {
//            GraphInit.givenAdaptedGraph_whenWriteBufferedImage_thenFileShouldExist();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        Double[] ans = CalImpact.getImpact(dataGatherManager);
        for(Double dou:ans)
            System.out.println(dou);

    }
}