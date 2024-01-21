package com.github.ryan6073.Seriously.BasicInfo;

import com.github.ryan6073.Seriously.Coefficient.CoefficientStrategy;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Impact.CalGraph;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.Graph;

import java.util.*;

public class DataGatherManager {//单例模式
    private static DataGatherManager mDataGatherManager = new DataGatherManager();
    public static DataGatherManager getInstance(){return mDataGatherManager;}
    public int authorNum;
    public int startYear,startMonth,finalYear,finalMonth;
    //新增，由用户自行设定，startYear和Month代表Graph累计包含至该时间点为止的引用关系，
    public CoefficientStrategy currentCoefficientStrategy;
    //finalYear和Month代表GraphItem的时间跨度，即从s.Year.Month到f.Year.Month这么多个月每个月都应有一个GraphItem
    public Map<Author,Vector<Paper>> dicAuthorPaper ;//作者的论文集合
    public Map<String,Paper> dicDoiPaper ;//doi->paperName,
    public Map<String,Author> dicOrcidAuthor ;//orcid->authorName
    public Map<Author,Vector<Paper>> dicEliteAuthorPaper ;//作者的精英论文集合
    public Map<String, Integer> dicOrcidMatrixOrder;//doi到矩阵下标的映射??????????????
    public Map<String, Double> dicJournalIF;//期刊与IF指数的映射
    public Map<TimeInfo,Vector<String>> dicTimeInfoDoi;//时间与相应论文的映射
    public Map<String,Journal> dicNameJournal;//JournalName和Journal的映射

    public Vector<Paper> papers;
    public Vector<Journal> journals;
    public Vector<Institution> institutions;

    public void initMatrixOrder(){
        dicOrcidMatrixOrder = new HashMap<>();
        int order=0;
        for(String orcid:dicOrcidAuthor.keySet()) {
            dicOrcidMatrixOrder.put(orcid, order);
            order++;
        }
    }
    public void initYearMonth(){
        LinkedList<TimeInfo> timeInfoList = new LinkedList<>(dicTimeInfoDoi.keySet());
        //升序排列
        Collections.sort(timeInfoList);
        int num=0;
        boolean flag = true;
        TimeInfo lastItem = null;
        for(TimeInfo item:timeInfoList){
            num+=dicTimeInfoDoi.get(item).size();
            if(num>=dicDoiPaper.size()/2 && flag){//即startyear startmonth之前的论文数（不包含startyear和startmonth本身）恰好大于或等于总数的一半
                startYear = item.year;
                startMonth = item.month;
                break;
            }
        }
        lastItem = timeInfoList.getLast();
        finalYear = lastItem.year;
        finalMonth = lastItem.month;
    }
    public void addJournal(Journal journal) {
        if(journals.contains(journal)) return;
        journals.add(journal);
        //这里我感觉同名的期刊没有被筛掉，可能需要再次遍历去除同名期刊并合并期刊的论文集合
    }
    public void addPaper(Paper paper) {
        if(papers.contains(paper)) return;
        papers.add(paper);
    }
    public void addInstitution(Institution institution) {
        if(institutions.contains(institution)) return;
        institutions.add(institution);
        //同上
    }
    public void addDicDP(Paper paper) {
        dicDoiPaper.put(paper.doi, paper);
    }
    public boolean paperFind(String doi){
        return dicDoiPaper.containsKey(doi);
    }
    public Paper paperGet(String doi){
        //遍历papers,找到doi对应的paper
        for(Paper paper:papers){
            if(paper.doi.equals(doi)) return paper;
        }
        return null;
    }
    public void addDicOA(Author author) {
        dicOrcidAuthor.put(author.orcid, author);
    }
    //    public void addDicAP(Author author, Paper paper) {
//        if (dicAuthorPaper.containsKey(author)) {
//            dicAuthorPaper.get(author).add(paper);
//        } else {
//            Vector<Paper> paperList = new Vector<>();
//            paperList.add(paper);
//            dicAuthorPaper.put(author, paperList);
//        }
//    }
    public void addDicAP(Author author,Vector<Paper> papers){
        dicAuthorPaper.put(author,papers);
    }
    public void addDicNJ(String journalName, Journal journal) {
        dicNameJournal.put(journalName, journal);
    }
    public void addDicEAP(Author author, Paper paper) {
        //遍历dicAuthorPaper找出作者对应的论文集合，然后把这篇论文加入到这个作者的精英论文集合中
        if (dicAuthorPaper.containsKey(author)) {
            dicEliteAuthorPaper.get(author).add(paper);
        }
    }
    public boolean journalFind(String paperJournal) {
        return dicNameJournal.containsKey(paperJournal);
    }

    public Journal journalGet(String paperJournal) {
        return dicNameJournal.get(paperJournal);
    }
    //添加作者-论文映射
    public Vector<Paper> getElite(Vector<Paper> papers){
        Vector<Paper> elitePapers = papers;
        //中间对影响力进行计算，选出精英论文集
        return elitePapers;
    }//根据dicAuthorPaper获得dicEliteAuthorPaper,我认为这个类型应该返回一个map，key是作者，value是论文集合，所以对于精英论文集我可以写成： dicEliteAuthorPaper = HashMap<Author,getElite(Vector(Paper))>();
    private DataGatherManager(){
        this.dicAuthorPaper = new HashMap<>();
        this.dicDoiPaper = new HashMap<>();
        this.dicOrcidAuthor = new HashMap<>();
        this.dicEliteAuthorPaper = new HashMap<>();
        this.dicOrcidMatrixOrder = new HashMap<>();//doi到矩阵下标的映射??????????????
        this.dicTimeInfoDoi = new HashMap<>();//时间与相应论文的映射
        this.currentCoefficientStrategy = new CoefficientStrategy();

        this.dicJournalIF = new HashMap<>();//期刊与IF指数的映射
        this.dicNameJournal = new HashMap<>();//JournalName和Journal的映射

        journals = new Vector<>();
        institutions = new Vector<>();
        papers = new Vector<>();
    }

    public boolean institutionFind(String authorInstitution) {
        for(Institution institution:institutions){
            if(institution.getInstitutionName().equals(authorInstitution)) return true;
        }
        return false;
    }

    public Institution institutionGet(String authorInstitution) {
        for(Institution institution:institutions){
            if(institution.getInstitutionName().equals(authorInstitution)) return institution;
        }
        return null;
    }
}