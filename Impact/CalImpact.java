package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.TimeInfo;
import com.github.ryan6073.Seriously.Coefficient.CoefficientStrategy;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CalImpact {

    static double [][] tempPapersImpact;

    //讲论文分为9类，求出每类论文的平均影响力数值
    static public void loadAveragePapersImpact(CoefficientStrategy coefficientStrategy){
        tempPapersImpact = coefficientStrategy.averagePapersImpact;
        int[][] numForm = new int[LevelManager.Level.levelNum][LevelManager.CitationLevel.citationLevelNum];
        for(int i=0;i<LevelManager.Level.levelNum;i++)
            for(int j=0;j<LevelManager.CitationLevel.citationLevelNum;j++) {
                numForm[i][j] = 0;
                tempPapersImpact[i][j] = 0.0;
            }
        for(Paper paper:DataGatherManager.getInstance().papers){
            LevelManager.Level level = paper.getLevel();
            LevelManager.CitationLevel citationLevel = CoefficientStrategy.getCitationLevel(paper);
            numForm[level.getIndex()][citationLevel.getIndex()]++;
            tempPapersImpact[level.getIndex()][citationLevel.getIndex()]+=paper.getPaperImpact();
        }
        for(int i=0;i<LevelManager.Level.levelNum;i++)
            for(int j=0;j<LevelManager.CitationLevel.citationLevelNum;j++) {
                if(numForm[i][j]!=0)
                    tempPapersImpact[i][j] = tempPapersImpact[i][j]/numForm[i][j];
                else{
                    tempPapersImpact[i][j] = 0.0;
                    System.out.println("存在空集的论文种类:"+" Level:"+i+" | "+ " CitationLevel:" + j);
                }
            }
    }
    //根据作者影响力计算论文影响力
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
    //根据pagerank算法计算作者影响力
    public static void initAuthorImpact(Vector<Double> graphImpact){
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        for(Map.Entry<String,Integer> entry:dataGatherManager.dicOrcidMatrixOrder.entrySet()){
            //更新作者影响力
            dataGatherManager.dicOrcidAuthor.get(entry.getKey()).setAuthorImpact(graphImpact.get(entry.getValue()));
        }
    }
    //计算所有论文的影响力
    public static void initPapersImpact(){
        Vector<Paper> papers = DataGatherManager.getInstance().papers;
        Iterator<Paper> paperIterator = papers.iterator();
        //一重循环遍历所有的论文，并依次计算它们的影响力大小
        while (paperIterator.hasNext()){
            Paper paperItem = paperIterator.next();
            paperItem.setPaperImpact(calPaperImpact(paperItem.getDoi()));
        }
    }
    //期刊影响力为平均论文影响力
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
    //机构影响力为平均作者影响力
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
    public static void updateAuthorImpact(Vector<Vector<String>> currentPapers){
        //对于引用网络的论文，其根据时间分为两类，一类已经脱离新手期，完全根据引用关系对作者影响力做出贡献
        //一类仍未成熟，引用关系和form值对于作者影响力各自按照权重比做出一定贡献
        //因此在更新过程中要进行加权运算
        //获得相应时间点的论文
        Vector<String> protectedPapers = currentPapers.get(0);
        //更新成熟引用图
        DirectedGraph<Author, Edge> matureGraph = GraphManager.getInstance().getMatureGraph();
        //更新作者影响力
        initAuthorImpact(CalGraph.getGraphImpact(matureGraph));
        //更新作者等级
        AuthorKMeans.AuthorKMeans(DataGatherManager.getInstance());
        //利用成熟论文的引用关系通过PageRank算法计算作者影响力
        //得到未成熟论文和其作者的等级，并利用ImpactForm，Form是否可以加一个论文寿命维度？
        for(String doi:protectedPapers){
            Paper paper = DataGatherManager.getInstance().paperGet(doi);
            LevelManager.Level paperLevel = paper.getLevel();
            for(String orcid:paper.getAuthorIDList()){
                Author author = DataGatherManager.getInstance().dicOrcidAuthor.get(orcid);
                LevelManager.Level authorLevel = author.getLevel();
                author.setAuthorImpact(author.getAuthorImpact()+ImpactForm.getInstance().getAuthorPaperImpact(authorLevel,paperLevel));
            }
        }
    }
    public static void updatePaperImpact(Vector<Vector<String>> currentPapers){
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        GraphManager graphManager = GraphManager.getInstance();
        //获得相应时间点的论文
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
            paper.setPaperImpact(dataGatherManager.currentCoefficientStrategy.getPaperImpactCoefficientExpectation(paper));
        }
        //对新增的成熟的论文影响力进行更新
        for(String doi:maturePapers){
            Paper paper = dataGatherManager.dicDoiPaper.get(doi);
            //根据自身等级和寿命设定影响力数值，成熟论文寿命为0，公式的相应参数可以通过同一个表达式给出
            double targetImpact = calPaperImpact(paper.getDoi());
            paper.setPaperImpact(targetImpact);
        }
    }
    public static void updateJournalImpact(){
        Iterator<Journal> journalIterator = DataGatherManager.getInstance().journals.iterator();
        while(journalIterator.hasNext()){
            Journal journalItem = journalIterator.next();
            Vector<String> paperDois = journalItem.getJournalPapers();
            double sumImpact=0.0;
            int paperNum=0;
            //将目标期刊的已读的成熟论文作为Journal的影响力计算因素
            for (String doi:paperDois){
                Paper paper = DataGatherManager.getInstance().dicDoiPaper.get(doi);
                if(paper.getIsRead()==1&&!paper.getIsAlive()) {
                    sumImpact += paper.getPaperImpact();
                    paperNum++;
                }
            }
            //平均论文影响力
            journalItem.setJournalImpact(sumImpact/paperNum);
        }
    }
    public static void updateInstitutionImpact(){
        // 此处存在问题，有些作者可能没有出现
        Iterator<Institution> institutionIterator = DataGatherManager.getInstance().institutions.iterator();
        while (institutionIterator.hasNext()){
            Institution institutionItem = institutionIterator.next();
            Vector<String> authors = institutionItem.getInstitutionAuthors();
            double sumImpact=0.0;
            int authorNum = 0;
            //将目标机构的已出现作者
            //作为Journal的影响力计算因素
            for(String orcid:authors){
                Author author = DataGatherManager.getInstance().dicOrcidAuthor.get(orcid);
                if(author.getIfExist()==1) {
                    sumImpact += author.getAuthorImpact();
                    authorNum++;
                }
            }
            //平均作者影响力
            institutionItem.setInstitutionImpact(sumImpact/authorNum);
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
        //更新母图并获取论文集
        Vector<Vector<String>> currentPapers = GraphManager.getInstance().updateGraph(year,month);
        //更新论文等级和影响力
        updatePaperImpact(currentPapers);
        //更新作者等级和影响力
        updateAuthorImpact(currentPapers);
        updateJournalImpact();
        updateInstitutionImpact();
    }
    public static Vector<Double> getImpact(DirectedGraph<Author, Edge> graph, DataGatherManager dataGatherManager){
        Vector<Double> graphImpact = CalGraph.getGraphImpact(graph);//
        initAll(dataGatherManager, graphImpact, dataGatherManager.startYear,dataGatherManager.startMonth);
        loadAveragePapersImpact(dataGatherManager.currentCoefficientStrategy);
        for(int i=dataGatherManager.startYear*12+dataGatherManager.startMonth;i<=dataGatherManager.finalYear*12+ dataGatherManager.finalMonth;i++) {
            updateAll(dataGatherManager,graphImpact,i/12,i%12);
            loadAveragePapersImpact(dataGatherManager.currentCoefficientStrategy);
        }
        //***
        return graphImpact;
    }
}

