package com.github.ryan6073.Seriously;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Graph.GraphStore;
import com.github.ryan6073.Seriously.Impact.CalImpact;

import java.util.Map;
import java.util.Vector;

public class Seriously {

    public static void main(String[] args) {
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        String file = "sample2.txt";
        FileInput.tempInit(dataGatherManager,file);
        System.out.println("完成文件初始化");

        //新增的初始化datagather的startyear/month finalyear/month
        dataGatherManager.initYearMonth();
        System.out.println("完成时间初始化");

        GraphManager graphManager = GraphManager.getInstance();

        //负责获取论文之间的引用关系用以构成图并初始化：各论文的被引用列表、各论文的引用等级、矩阵
        GraphInit.initGraph(graphManager,dataGatherManager,dataGatherManager.firstYear,dataGatherManager.firstMonth);

//        GraphStore.store("0000-0", graphManager.Graph);
//        System.out.println("完成初始总图的存储");

        //直接从start的下一个月开始更新作者的影响力
        for(int i=dataGatherManager.startYear*12+dataGatherManager.startMonth+1;i<=dataGatherManager.finalYear*12+ dataGatherManager.finalMonth+12;i++) {
            //注意在startyear startmonth的时候就开始更新了
            int year,month;
            if(i%12==0){
                month = 12;
                year = i/12-1;
            }
            else{
                month = i%12;
                year = i/12;
            }
            //更新母图并获取论文集
            Vector<Vector<String>> currentPapers = GraphManager.getInstance().updateGraph(year,month);
        }
        for(Map.Entry<String,Author> entry:dataGatherManager.dicOrcidAuthor.entrySet()){
            System.out.println(entry.getValue().getAuthorName()+' '+entry.getValue().getAuthorImpact()+' '+entry.getValue().getLevel());
        }

    }
}