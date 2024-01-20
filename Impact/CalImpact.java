package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphManager;

import java.util.*;

public class CalImpact {

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
    public static void initAuthorImpact(){
        Vector<Double> graphImpact = CalGraph.getGraphImpact(GraphManager.getInstance().getMatureGraph());
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        for(Map.Entry<String,Integer> entry:dataGatherManager.dicOrcidMatrixOrder.entrySet()){
            //更新作者影响力
            dataGatherManager.dicOrcidAuthor.get(entry.getKey()).setAuthorImpact(graphImpact.get(entry.getValue()));
        }
    }
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
        //更新作者影响力
        initAuthorImpact();
        //更新作者等级
        AuthorKMeans.AuthorKMeans(DataGatherManager.getInstance());
        //利用成熟论文的引用关系通过PageRank算法计算作者影响力
        //得到未成熟论文和其作者的等级，并利用ImpactForm，Form是否可以加一个论文寿命维度？
        for(String doi:protectedPapers){
            Paper paper = DataGatherManager.getInstance().paperGet(doi);
            LevelManager.Level paperLevel = paper.getLevel();
            double[] stateDistribution = DataGatherManager.getInstance().currentCoefficientStrategy.getStateDistribution(paper);
            for(String orcid:paper.getAuthorIDList()){
                Author author = DataGatherManager.getInstance().dicOrcidAuthor.get(orcid);
                LevelManager.Level authorLevel = author.getLevel();
                double tempImpact = 0.0;
                for(int i=0;i<LevelManager.CitationLevel.citationLevelNum;i++)
                    tempImpact+=stateDistribution[i]*ImpactForm.getInstance().getAuthorPaperImpact(authorLevel,paperLevel,LevelManager.CitationLevel.getCitationLevelByIndex(i));
                author.setAuthorImpact(author.getAuthorImpact()+tempImpact);
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
            double targetImpact = calPaperImpact(paper.getDoi());
            paper.setPaperImpact(targetImpact);
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
    public static void initAll(){
        initAuthorImpact();
        initPapersImpact();
        initJournalImpact();
        initInstitutionImpact();
    }
    public static void updateAll(int year, int month){
        //更新母图并获取论文集
        Vector<Vector<String>> currentPapers = GraphManager.getInstance().updateGraph(year,month);
        //更新作者等级和影响力
        updateAuthorImpact(currentPapers);
        //更新论文等级和影响力
        updatePaperImpact(currentPapers);
        updateJournalImpact();
        updateInstitutionImpact();
    }
    public static Double[] getImpact(DataGatherManager dataGatherManager){

        //初始化的时候作者影响力只考虑成熟的引用关系，update的时候作者影响力再考虑不成熟的引用关系
        initAll();
        for(int i=dataGatherManager.startYear*12+dataGatherManager.startMonth;i<=dataGatherManager.finalYear*12+ dataGatherManager.finalMonth;i++) {
            //注意在startyear startmonth的时候就开始更新了
            int year,month;
            if(i%12==0){
                month = 12;
                year = i/12-1;
            }
            else{
                month = i%12;
                year = i/12;
            }
            updateAll(year,month);
        }
        Double[] ans = new Double[dataGatherManager.authorNum];
        for(int i=0;i< dataGatherManager.authorNum;i++)
            ans[i]=-1.0;
        for(Map.Entry<String,Author> entry:dataGatherManager.dicOrcidAuthor.entrySet()){
            int order = dataGatherManager.dicOrcidMatrixOrder.get(entry.getKey());
            if(entry.getValue().getIfExist()==1)
                ans[order] = entry.getValue().getAuthorImpact();
        }
        return ans;
    }
}

