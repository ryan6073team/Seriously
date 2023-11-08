package com.github.ryan6073.Seriously;

//import com.github.ryan6073.Seriously.BasicInfo.BasicDataInit;
import com.github.ryan6073.Seriously.BasicInfo.AuthorKMeans;
import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.BasicInfo.JournalKMeans;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import com.github.ryan6073.Seriously.BasicInfo.FileInput;

import java.io.IOException;
import java.util.Vector;

import static com.github.ryan6073.Seriously.BasicInfo.FileInput.initJournalToIF;

public class Seriously {//123456
    public static void main(String[] args) {
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        GraphManager graphManager = GraphManager.getInstance();
        DataInit.initDate(dataGatherManager, graphManager);
        Vector<Double> ans = CalImpact.getImpact(graphManager.Graph,dataGatherManager);
        //test
    }
}