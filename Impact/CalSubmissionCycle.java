package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.BasicInfo.Paper;
import java.util.Vector;

public class CalSubmissionCycle {
    public static Vector<Double> getRecCycleImpact(DataGatherManager dataGatherManager){
        return null;
    }
    public static Vector<Double> getAccCycleImpact(DataGatherManager dataGatherManager){
        return null;
    }
    public static Vector<Double> getRevCycleImpact(DataGatherManager dataGatherManager){
        return null;
    }
    public static Vector<Double> getPubCycleImpact(DataGatherManager dataGatherManager){
        return null;
    }
    public static Vector<Double> getSubmissionCycleImpact(DataGatherManager dataGatherManager){
        Vector<Paper> recPaperVector = dataGatherManager.recPapers;
        Vector<Paper> accPaperVector = dataGatherManager.accPapers;
        Vector<Paper> revPaperVector = dataGatherManager.revPapers;
        Vector<Paper> pubPaperVector = dataGatherManager.pubPapers;
        Vector<Double> recImpact = getRecCycleImpact(dataGatherManager);//received
        Vector<Double> accImpact = getAccCycleImpact(dataGatherManager);//accepted
        Vector<Double> revImpact = getRevCycleImpact(dataGatherManager);//revised
        Vector<Double> CycleImpact = new Vector<>();
        if (revImpact != null) {
            for(int i=0;i<revImpact.size();i++)
                CycleImpact.add(revImpact.get(i)+accImpact.get(i)+recImpact.get(i));
        }
        return CycleImpact;
    }
}
