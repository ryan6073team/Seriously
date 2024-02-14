package com.github.ryan6073.Seriously;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Graph.GraphStore;
import com.github.ryan6073.Seriously.Impact.CalImpact;

import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

public class Seriously {

    public static void main(String[] args) {
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        String file = "author_net.txt";
        FileInput.tempInit(dataGatherManager,file);
        System.out.println("完成文件初始化");

        //新增的初始化datagather的startyear/month finalyear/month
        dataGatherManager.initYearMonth();
        System.out.println("完成时间初始化");

        GraphManager graphManager = GraphManager.getInstance();

        //负责获取论文之间的引用关系用以构成图并初始化：各论文的被引用列表、各论文的引用等级、矩阵
        GraphInit.initGraph(graphManager,dataGatherManager,dataGatherManager.firstYear,dataGatherManager.firstMonth);
        System.out.println("第一年："+dataGatherManager.firstYear+" 第一个月："+dataGatherManager.firstMonth);

//        System.out.println("请输入年份和月份：（从第一年的第二个月开始）");
//        int scyear,scmonth;
//        Scanner scanner = new Scanner(System.in);
//        scyear = scanner.nextInt();
//        scmonth = scanner.nextInt();

        //直接从start的下一个月开始更新作者的影响力，当时间推移到published year published month时，相应时间发表的论文life为1
        for(int i=dataGatherManager.firstYear*12+dataGatherManager.firstMonth+1;i<=dataGatherManager.finalYear*12+ dataGatherManager.finalMonth+13;i++) {
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
//            if(year==scyear&&month==scmonth) {
//                GraphStore.storeGraph(year + "---" + month, GraphManager.getInstance().Graph);
//                break;
//            }
            System.out.println();
        }
        //彻底关闭driver
        GraphStore.getInstance().closeDriver();
        for(Map.Entry<String,Author> entry:dataGatherManager.dicOrcidAuthor.entrySet()){
            System.out.println(entry.getValue().getAuthorName()+' '+entry.getValue().getAuthorImpact()+' '+entry.getValue().getLevel());
        }

    }
}