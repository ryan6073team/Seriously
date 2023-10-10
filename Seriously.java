package com.github.ryan6073.Seriously;

//import com.github.ryan6073.Seriously.BasicInfo.BasicDataInit;
import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import com.github.ryan6073.Seriously.BasicInfo.FileInput;

import java.util.Vector;

public class Seriously {
    public static void main(String[] args) {
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        //BasicDataInit.initDate(dataGatherManager);  我的fileinput已经把这个初始化的工作做了，所以这里不需要再初始化了
        FileInput.init(dataGatherManager);
        //打印paper1的作者列表
        //System.out.println(dataGatherManager.papers.indexOf(dataGatherManager.dicDoiPaper.get("10.1109/ICECET55527.2022.9872831")));
        System.out.println(dataGatherManager.dicDoiPaper.get("10.1109/ICECET55527.2022.9872831").getAuthorIDList());
//        GraphManager graphManager = GraphManager.getInstance();
//        GraphInit.initGraph(graphManager,dataGatherManager);
//        Vector<Double> ans = CalImpact.getImpact(graphManager,dataGatherManager);
    }
}