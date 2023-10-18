package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphManager;

import java.util.Iterator;
import java.util.Vector;

public class CalImpact {
    public static void updateAuthorImpact(Vector<Double> graphImpact){
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        for(int i=0;i<graphImpact.size();i++) {
            for (String orcid : dataGatherManager.dicOrcidMatrixOrder.keySet()){
                if(dataGatherManager.dicOrcidMatrixOrder.get(orcid)==i){
                    //更新作者影响力
                    dataGatherManager.dicOrcidAuthor.get(orcid).setAuthorImpact(graphImpact.get(i));
                }
            }
        }
    }
    public static void updatePaperImpact(){
        Vector<Paper> papers = DataGatherManager.getInstance().pubPapers;
        Iterator<Paper> paperIterator = papers.iterator();
        while(paperIterator.hasNext()){
            Paper paperItem = paperIterator.next();
            Vector<String> authors = paperItem.getAuthorIDList();
            Double sumImpact = 0.0;
            for(String orcid:authors){
                sumImpact+=DataGatherManager.getInstance().dicOrcidAuthor.get(orcid).getAuthorImpact();
            }
            paperItem.setPaperImpact(sumImpact);
        }
    }
    public static void updateJournalImpact(){
        Iterator<Journal> journalIterator = DataGatherManager.getInstance().journals.iterator();
        while(journalIterator.hasNext()){
            Journal journalItem = journalIterator.next();
            Vector<String> paperDois = journalItem.getJournalPapers();
            Double sumImpact=0.0;
            for (String doi:paperDois){
                sumImpact+=DataGatherManager.getInstance().dicDoiPaper.get(doi).getPaperImpact();
            }
            //平均论文影响力
            journalItem.setJournalImpact(sumImpact/journalItem.getJournalPapers().size());
        }
    }
    public static void updateInstitutionImpact(){
        Iterator<Institution> institutionIterator = DataGatherManager.getInstance().institutions.iterator();
        while (institutionIterator.hasNext()){
            Institution institutionItem = institutionIterator.next();
            Vector<String> authors = institutionItem.getInstitutionAuthors();
            Double sumImpact=0.0;
            for(String orcid:authors){
                sumImpact+=DataGatherManager.getInstance().dicOrcidAuthor.get(orcid).getAuthorImpact();
            }
            //平均作者影响力
            institutionItem.setInstitutionImpact(sumImpact/institutionItem.getInstitutionAuthors().size());
        }
    }
    public static Vector<Double> getImpact(GraphManager graphManager, DataGatherManager dataGatherManager){
        dataGatherManager.initMatrixOrder();
        Vector<Double> graphImpact = CalGraph.getGraphImpact(graphManager);
        updateAuthorImpact(graphImpact);
        updatePaperImpact();
        updateJournalImpact();
        updateInstitutionImpact();
        Vector<Double> submissionCycleImpact = CalSubmissionCycle.getSubmissionCycleImpact(dataGatherManager);
        //***
        return graphImpact;
    }
}
