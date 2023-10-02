package com.github.ryan6073.Seriously;

import com.github.ryan6073.Seriously.BasicInfo.BasicDataInit;
import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Impact.CalImpact;

import java.util.Vector;

public class Seriously {
    public static void main(String[] args) {
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        BasicDataInit.initDate(dataGatherManager);
        GraphManager graphManager = GraphManager.getInstance();
        GraphInit.initGraph(graphManager,dataGatherManager);
        Vector<Double> ans = CalImpact.getImpact(graphManager,dataGatherManager);
    }
}
