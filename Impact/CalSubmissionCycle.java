package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.BasicInfo.Paper;
import java.util.Vector;

public class CalSubmissionCycle {
    public static Vector<Double> getRecCycleImpact(DataGatherManager dataGatherManager){
        /*对于接受到的文章，由于还没被编辑审核通过，因此文章不应该被赋予机构的影响力值，
        考量到文章由多个作者撰写，此时文章的影响力值应为文章各作者的影响力平均值*/
        return null;
    }
    public static Vector<Double> getAccCycleImpact(DataGatherManager dataGatherManager){
        return null;
    }
    public static Vector<Double> getRevCycleImpact(DataGatherManager dataGatherManager){
        return null;
    }
    public static Vector<Double> getSubmissionCycleImpact(DataGatherManager dataGatherManager){
        /*在进入周期影响力的计算函数之前，网络中各作者、期刊、文章、机构的影响力已被算出*/
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
