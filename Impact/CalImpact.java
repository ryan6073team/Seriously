package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

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
        Vector<Paper> papers = DataGatherManager.getInstance().papers;
        Iterator<Paper> paperIterator = papers.iterator();
        //一重循环遍历所有的论文，并依次计算它们的影响力大小
        while (paperIterator.hasNext()){
            Paper paperItem = paperIterator.next();
            Vector<String> citedDois = paperItem.getCitedList();
            double sumImpact = 0.0;
            //二重循环遍历被引论文，计算并累加它们的平均作者影响力
            for(String doi:citedDois){
                Paper citedPaper = DataGatherManager.getInstance().dicDoiPaper.get(doi);
                Vector<String> authors = citedPaper.getAuthorIDList();
                Double citedPaperImpact = 0.0;
                for(String orcid:authors){
                    citedPaperImpact+=DataGatherManager.getInstance().dicOrcidAuthor.get(orcid).getAuthorImpact();
                }
                citedPaperImpact = citedPaperImpact/authors.size();
                sumImpact+=citedPaperImpact;
            }
            paperItem.setPaperImpact(sumImpact);
        }
    }
    public static void updateJournalImpact(){
        Iterator<Journal> journalIterator = DataGatherManager.getInstance().journals.iterator();
        while(journalIterator.hasNext()){
            Journal journalItem = journalIterator.next();
            Vector<String> paperDois = journalItem.getJournalPapers();
            double sumImpact=0.0;
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
            double sumImpact=0.0;
            for(String orcid:authors){
                sumImpact+=DataGatherManager.getInstance().dicOrcidAuthor.get(orcid).getAuthorImpact();
            }
            //平均作者影响力
            institutionItem.setInstitutionImpact(sumImpact/institutionItem.getInstitutionAuthors().size());
        }
    }
    public static void calGraphItemImpact(DataGatherManager dataGatherManager,DirectedGraph<Author,Edge> graphItem,Vector<Double> graphImpact,int year,int month){
        //实例化并调用相论文处理类
        Vector<String> thenPaperDois = dataGatherManager.dicTimeInfoDoi.get(new TimeInfo(year,month));
        if(thenPaperDois==null)//没有相应的时间数据
            return;
        CalPapers calPapers = new CalPapers(thenPaperDois);
        calPapers.excute();
    }
    public static Vector<Double> getImpact(GraphManager graphManager, DataGatherManager dataGatherManager){
        dataGatherManager.initMatrixOrder();
        Vector<Double> graphImpact = CalGraph.getGraphImpact(graphManager);
        for(int i=dataGatherManager.startYear*12+dataGatherManager.startMonth;i<=dataGatherManager.finalYear*12+ dataGatherManager.finalMonth;i++) {
            updateAuthorImpact(graphImpact);
            updatePaperImpact();
            updateJournalImpact();
            updateInstitutionImpact();
            DirectedGraph<Author,Edge> graphItem = GraphManager.getInstance().getGraphItem(i/12,i%12);
            calGraphItemImpact(dataGatherManager,graphItem,graphImpact,i/12,i%12);
        }
        //***
        return graphImpact;
    }
}
