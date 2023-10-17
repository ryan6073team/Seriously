package com.github.ryan6073.Seriously;

//import com.github.ryan6073.Seriously.BasicInfo.BasicDataInit;
import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import com.github.ryan6073.Seriously.BasicInfo.FileInput;

import java.io.IOException;
import java.util.Vector;

public class Seriously {//123456
    public static void main(String[] args) {
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        //BasicDataInit.initDate(dataGatherManager);  我的fileinput已经把这个初始化的工作做了，所以这里不需要再初始化了
        FileInput.init(dataGatherManager);
        System.out.println(dataGatherManager.dicAuthorPaper.size());//测试
        GraphManager graphManager = GraphManager.getInstance();
        GraphInit.initGraph(graphManager,dataGatherManager);
        try {
            GraphInit.givenAdaptedGraph_whenWriteBufferedImage_thenFileShouldExist();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Vector<Double> ans = CalImpact.getImpact(graphManager,dataGatherManager);
        //test
    }
}