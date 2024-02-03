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
    public static void calAuthorsMatureImpact(){
        Vector<Double> graphImpact = CalGraph.getGraphImpact(GraphManager.getInstance().getMatureGraph());
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        for(Map.Entry<String,Integer> entry:dataGatherManager.dicOrcidMatrixOrder.entrySet()){
            //更新作者影响力
            dataGatherManager.dicOrcidAuthor.get(entry.getKey()).setAuthorImpact(graphImpact.get(entry.getValue()));
        }
    }
    public static void calAuthorsMatureImpact(Vector<Double> graphImpact){
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        for(Map.Entry<String,Integer> entry:dataGatherManager.dicOrcidMatrixOrder.entrySet()){
            //更新作者影响力
            dataGatherManager.dicOrcidAuthor.get(entry.getKey()).setAuthorImpact(graphImpact.get(entry.getValue()));
        }
    }
    //计算所有论文的影响力
    public static void initPapersImpact() {
        for (Paper paper : DataGatherManager.getInstance().papers)
            //已经被遍历的论文才有资格被初始化影响力，注意：存在于图中的论文是已经被遍历的论文的子集
            if (paper.getIsRead() == 1)
                paper.setPaperImpact(calPaperImpact(paper.getDoi()));
    }

    //期刊影响力为平均论文影响力
    public static void initJournalImpact(){
        for(Journal journal:DataGatherManager.getInstance().journals){
            if(journal.getIfExist()==0)
                continue;
            double sumImpact=0.0;
            int currentPapersNum=0;
            for (String doi:journal.getJournalPapers()){
                Paper paper = DataGatherManager.getInstance().dicDoiPaper.get(doi);
                if(paper.getIsRead()==1) {
                    sumImpact += DataGatherManager.getInstance().dicDoiPaper.get(doi).getPaperImpact();
                    currentPapersNum++;
                }
            }
            if(currentPapersNum!=0)
            //平均论文影响力
                journal.setJournalImpact(sumImpact/currentPapersNum);
            else journal.setJournalImpact(0.0);
        }
    }
    //机构影响力为平均作者影响力
    public static void initInstitutionImpact(){
        int currentAuthorsNum = 0;
        for(Institution institutionItem:DataGatherManager.getInstance().dicNameInstitutions.values()){
            Vector<String> authors = institutionItem.getInstitutionAuthors();
            double sumImpact=0.0;
            for(String orcid:authors){
                if(DataGatherManager.getInstance().dicOrcidAuthor.get(orcid).getIfExist()==1) {
                    sumImpact += DataGatherManager.getInstance().dicOrcidAuthor.get(orcid).getAuthorImpact();
                    currentAuthorsNum++;
                }
            }
            if(currentAuthorsNum!=0)
            //平均作者影响力
                institutionItem.setInstitutionImpact(sumImpact/currentAuthorsNum);
            else institutionItem.setInstitutionImpact(0.0);
        }
    }
    public static void initorUpdateAuthorImpact(Vector<Vector<String>> currentPapers){
        //对于引用网络的论文，其根据时间分为两类，一类已经脱离新手期，完全根据引用关系对作者影响力做出贡献
        //一类仍未成熟，引用关系和form值对于作者影响力各自按照权重比做出一定贡献
        //因此在更新过程中要进行加权运算
        //获得相应时间点的论文
        Vector<String> protectedPapers = currentPapers.get(0);
        //更新作者影响力
        calAuthorsMatureImpact();
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
                //若出现作者在遍历这篇论文之前没有被算过
                if(author.getAuthorImpact()==-1.0)
                    author.setAuthorImpact(0.0);
                author.setAuthorImpact(author.getAuthorImpact()+tempImpact);
            }
        }
    }
    //更新全部现存论文的影响力
    public static void updatePaperImpact(){
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        //获得全部现存论文
        Vector<Paper> targetPapers = new Vector<>();
        for(Paper paper: dataGatherManager.papers){
            if(paper.getIsRead()==1)
                targetPapers.add(paper);
        }
        for(Paper paper:targetPapers){
            double targetImpact = calPaperImpact(paper.getDoi());
            paper.setPaperImpact(targetImpact);
        }
    }
    public static void updateJournalImpact(){
        for(Journal journalItem:DataGatherManager.getInstance().journals){
            if(journalItem.getIfExist()==0)
                continue;
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
            if(paperNum!=0)
            //平均论文影响力
                journalItem.setJournalImpact(sumImpact/paperNum);
            else journalItem.setJournalImpact(0.0);
        }
    }
    public static void updateInstitutionImpact(){
        // 根据现存作者进行机构影响力的计算
        for(Institution institutionItem:DataGatherManager.getInstance().dicNameInstitutions.values()){
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
            if(authorNum!=0)
                //平均作者影响力
                institutionItem.setInstitutionImpact(sumImpact/authorNum);
            else institutionItem.setInstitutionImpact(0.0);
        }
    }
    public static void initAll(){
        calAuthorsMatureImpact();
        initPapersImpact();
        initJournalImpact();
        initInstitutionImpact();
    }
    public static void updateAll(int year, int month){
        //更新母图并获取论文集
        Vector<Vector<String>> currentPapers = GraphManager.getInstance().updateGraph(year,month);
        //更新作者等级和影响力
        initorUpdateAuthorImpact(currentPapers);
        //更新论文等级和影响力
        updatePaperImpact();
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

