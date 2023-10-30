package com.github.ryan6073.Seriously.BasicInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
public class DataGatherManager {//单例模式
    private static DataGatherManager mDataGatherManager = new DataGatherManager();
    public static DataGatherManager getInstance(){return mDataGatherManager;}
    public int authorNum;
    public int startYear,startMonth,finalYear,finalMonth;
    //新增，由用户自行设定，startYear和Month代表Graph累计包含至该时间点为止的引用关系，
    //finalYear和Month代表GraphItem的时间跨度，即从s.Year.Month到f.Year.Month这么多个月每个月都应有一个GraphItem
    public Map<Author,Vector<Paper>> dicAuthorPaper ;//作者的论文集合
    public Map<String,Paper> dicDoiPaper ;//doi->paperName,
    public Map<String,Author> dicOrcidAuthor ;//orcid->authorName
    public Map<Author,Vector<Paper>> dicEliteAuthorPaper ;//作者的精英论文集合
    public Map<String, Integer> dicOrcidMatrixOrder;//doi到矩阵下标的映射
    public Map<String, Double> dicJournalIF;//期刊与IF指数的映射
    public Vector<Paper> papers;
    public Vector<Journal> journals;
    public Vector<Institution> institutions;

    //暂时辅助矩阵用
    public void initMatrixOrder(){
        dicOrcidMatrixOrder = new HashMap<>();
        int order=0;
        for(String orcid:dicOrcidAuthor.keySet()) {
            dicOrcidMatrixOrder.put(orcid, order);
            order++;
        }
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
    public void addDicDA(Author author,Vector<Paper> papers){
        dicAuthorPaper.put(author,papers);
    }
    public void addDicEAP(Author author, Paper paper) {
        //遍历dicAuthorPaper找出作者对应的论文集合，然后把这篇论文加入到这个作者的精英论文集合中
        if (dicAuthorPaper.containsKey(author)) {
            dicEliteAuthorPaper.get(author).add(paper);
        }
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
        journals = new Vector<>();
        institutions = new Vector<>();
        papers = new Vector<>();
    }

}
//arraylist和vector的区别
//arraylist是非线程安全的，vector是线程安全的
//arraylist是异步的，vector是同步的
//arraylist是不安全的，vector是安全的
//arraylist是快速的，vector是慢速的
//arraylist是轻量级的，vector是重量级的
//部分vector确实可以用arraylist，比如institutions，journals，但是dicAuthorPaper，dicDoiPaper，dicOrcidAuthor，dicEliteAuthorPaper，recPapers，accPapers，revPapers，pubPapers，这些都是需要线程安全的，所以用vector比较好