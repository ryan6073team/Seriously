package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Vector;

public class Author {
    String authorName,orcid;
    Vector<String> authorInstitution;
    Double authorImpact = 0.0;
    boolean flag;//是否存在于数据源中
    public Author(String _authorName,String _orcid,String _authorInstitution){
        authorName = _authorName;
        orcid = _orcid;
        flag = true;//既然被有参构造，肯定是存在于数据源头中吧？
        authorInstitution = new Vector<>();
        authorInstitution.add(_authorInstitution);
    }
    public Author(String _authorName,String _orcid,Vector<String> _authorInstitution){
        authorName = _authorName;
        orcid = _orcid;
        flag = true;//既然被有参构造，肯定是存在于数据源头中吧？
        authorInstitution = _authorInstitution;
    }
//    public void addPaper(String _authorName,String _doi){
//        //用_authorName来找到作者，然后把这篇论文的DOI加入到这个作者的论文集合中
//        //遍历Authors，找到作者，然后把这篇论文的DOI加入到这个作者的论文集合中
//        for(Author author:DataGatherManager.getInstance().dicAuthorPaper.keySet()){
//            if(author.authorName.equals(_authorName)){
//                author.papers.add(DataGatherManager.getInstance().dicDoiPaper.get(_doi));
//                break;
//            }
//        }
//    }
    //我觉得需要一个papers向量数据成员，来存放一个作者的文章集合
    public boolean getFlag(){return flag;}
    public String getOrcid(){return orcid;}
    public String getAuthorName(){return authorName;}
    public Double getAuthorImpact(){return authorImpact;}
    public void setAuthorImpact(Double _authorImpact){authorImpact = _authorImpact;}
}
