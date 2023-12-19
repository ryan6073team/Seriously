package com.github.ryan6073.Seriously;

//import com.github.ryan6073.Seriously.BasicInfo.BasicDataInit;
import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Graph.GraphStore;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static com.github.ryan6073.Seriously.BasicInfo.FileInput.initJournalToIF;

public class Seriously {
    public static void main(String[] args) {
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();

//        initJournalToIF(dataGatherManager);//更新期刊IF的映射

        FileInput.init(dataGatherManager);
//        System.out.println(dataGatherManager.dicAuthorPaper.size());//测试
        //新增的初始化datagather的startyear/month finalyear/month
        dataGatherManager.initYearMonth();
        //更新等级
        AuthorKMeans.AuthorKMeans(dataGatherManager);
        JournalKMeans.JournalkMeans(dataGatherManager);
        GraphManager graphManager = GraphManager.getInstance();
        GraphInit.initGraph(graphManager,dataGatherManager,dataGatherManager.startYear,dataGatherManager.startMonth);
        GraphInit.initGraphItems(graphManager,dataGatherManager,dataGatherManager.startYear,dataGatherManager.startMonth,dataGatherManager.finalYear,dataGatherManager.finalMonth);
        try {
            GraphInit.givenAdaptedGraph_whenWriteBufferedImage_thenFileShouldExist();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Vector<Double> ans = CalImpact.getImpact(graphManager.Graph,dataGatherManager);
//        for(Double dou:ans)
//            System.out.println(dou);

    }
}