package com.github.ryan6073.Seriously.src.Impact;

import com.github.ryan6073.Seriously.src.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.src.Graph.GraphManager;

import java.util.Vector;

public class CalImpact {
    public static Vector<Double> getImpact(GraphManager graphManager, DataGatherManager dataGatherManager){
        Vector<Double> graphImpact = CalGraph.getGraphImpact(graphManager);
        Vector<Double> submissionCycleImpact = CalSubmissionCycle.getSubmissionCycleImpact(dataGatherManager);
        //***
        return null;
    }
}
