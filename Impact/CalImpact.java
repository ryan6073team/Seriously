package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

import java.util.Iterator;
import java.util.Vector;

public class CalImpact {
    public static double calPaperImpact(String _doi){
        Paper paper = DataGatherManager.getInstance().dicDoiPaper.get(_doi);
        Vector<String> citedDois = paper.getCitedList();
        double sumImpact = 0.0;
        //遍历被引论文，计算并累加它们的平均作者影响力
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
        return sumImpact;
    }
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
    public static void initPapersImpact(){
        Vector<Paper> papers = DataGatherManager.getInstance().papers;
        Iterator<Paper> paperIterator = papers.iterator();
        //一重循环遍历所有的论文，并依次计算它们的影响力大小
        while (paperIterator.hasNext()){
            Paper paperItem = paperIterator.next();
            paperItem.setPaperImpact(calPaperImpact(paperItem.getDoi()));
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
    public static void updatePaperImpact(int year, int month, PaperCoefficientStrategy strategy){
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        GraphManager graphManager = GraphManager.getInstance();
        //获得相应时间点的论文
            //更新母图和论文并获取论文集
        Vector<Vector<String>> currentPapers = GraphManager.getInstance().updateGraph(year,month);
        Vector<String> maturePapers = currentPapers.get(1);
        Vector<String> protectedPapers = currentPapers.get(0);
        //对它们进行分级，给予影响力
            //更新期刊等级
        JournalKMeans.JournalkMeans(dataGatherManager);
            //给保护期论文相应的影响力
        for(String doi:protectedPapers){
            Paper paper = dataGatherManager.dicDoiPaper.get(doi);
            //将论文所处期刊的等级设为自身等级
            paper.setLevel(LevelManager.RanktoLevel(dataGatherManager.dicNameJournal.get(paper.getJournal()).getRank()));
            //根据自身等级和寿命设定影响力数值
            double targetImpact = strategy.getCitingCoefficient(paper.getLifeSpan())*calPaperImpact(paper.getDoi())+strategy.getLevelCoefficient(paper.getLifeSpan())*Journal.levelImpact.get(paper.getLevel());
            paper.setPaperImpact(targetImpact);
        }
        //对新增的成熟的论文影响力进行更新
        for(String doi:maturePapers){
            Paper paper = dataGatherManager.dicDoiPaper.get(doi);
            //根据自身等级和寿命设定影响力数值，成熟论文寿命为0，公式的相应参数可以通过同一个表达式给出
            double targetImpact = strategy.getCitingCoefficient(paper.getLifeSpan())*calPaperImpact(paper.getDoi())+strategy.getLevelCoefficient(paper.getLifeSpan())*Journal.levelImpact.get(paper.getLevel());
            paper.setPaperImpact(targetImpact);
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
    public static void initAll(DataGatherManager dataGatherManager, Vector<Double> graphImpact, int year, int month){
        initAuthorImpact(graphImpact);
        initPapersImpact();
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

class PaperCoefficientStrategy{
    public double getLevelCoefficient(int life/*参与保护期(月)*/){
        return life/12.0;
    }
    public double getCitingCoefficient(int life){
        return 1-life/12.0;
    }
}
