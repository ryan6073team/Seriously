package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

import java.util.Iterator;
import java.util.Vector;

public class CalImpact {
    public static void initAuthorImpact(Vector<Double> graphImpact){
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
    public static void initPaperImpact(){
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
    public static void initJournalImpact(){
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
    public static void initInstitutionImpact(){
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
    public static void updateAuthorImpact(DirectedGraph<Author, Edge> graph){
        //对于引用网络的论文，其根据时间分为两类，一类已经脱离新手期，完全根据引用关系对作者影响力做出贡献
        //一类仍未成熟，引用关系和form值对于作者影响力各自按照权重比做出一定贡献
        //因此在更新过程中要进行加权运算
    }
    public static void updatePaperImpact(){
        //获得相应时间点的论文
        //对它们进行分级，给予影响力
        //合并Item到母网络
        //对过去时间点的论文影响力进行更新
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
    public static void initAll(DataGatherManager dataGatherManager, Vector<Double> graphImpact, int year, int month){
        initAuthorImpact(graphImpact);
        initPaperImpact();
        initJournalImpact();
        initInstitutionImpact();
        DirectedGraph<Author,Edge> graphItem = GraphManager.getInstance().getGraphItem(year,month);
        calGraphItemImpact(dataGatherManager,graphItem,graphImpact,year,month);
    }
    public static void updateAll(DataGatherManager dataGatherManager, Vector<Double> graphImpact, int year, int month){

    }
    public static Vector<Double> getImpact(DirectedGraph<Author, Edge> graph, DataGatherManager dataGatherManager){
        Vector<Double> graphImpact = CalGraph.getGraphImpact(graph);
        initAll(dataGatherManager, graphImpact, dataGatherManager.startYear,dataGatherManager.startMonth);
        for(int i=dataGatherManager.startYear*12+dataGatherManager.startMonth;i<=dataGatherManager.finalYear*12+ dataGatherManager.finalMonth;i++) {
            updateAll(dataGatherManager,graphImpact,i/12,i%12);
        }
        //***
        return graphImpact;
    }
}
