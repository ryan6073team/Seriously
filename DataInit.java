package com.github.ryan6073.Seriously;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;

import java.io.IOException;

import static com.github.ryan6073.Seriously.BasicInfo.FileInput.initJournalToIF;

public class DataInit {
    public static void initDate(DataGatherManager dataGatherManager, GraphManager graphManager){
        initJournalToIF(dataGatherManager);//更新期刊IF的映射
        FileInput.init(dataGatherManager);//针对文件输入进行数据更新
        System.out.println(dataGatherManager.dicAuthorPaper.size());//测
        GraphInit.initGraph(graphManager,dataGatherManager,dataGatherManager.startYear,dataGatherManager.startMonth);//初始化母图
        GraphInit.initGraphItems(graphManager,dataGatherManager,dataGatherManager.startYear,dataGatherManager.startMonth,dataGatherManager.finalYear,dataGatherManager.finalMonth);
        //初始化子图
        try {
            GraphInit.givenAdaptedGraph_whenWriteBufferedImage_thenFileShouldExist();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AuthorKMeans.AuthorKMeans(dataGatherManager);//根据当前母图，更新authorrank
        JournalKMeans.JournalkMeans(dataGatherManager);//根据当前母图，更新journalrank
        Journal.updateLevelImpact();//更新
    }//初始化dataGatherManager
}
