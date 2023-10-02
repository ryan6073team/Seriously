package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Vector;

public class Author {
    String authorName,orcid;
    Vector<String> authorInstitution;
    boolean flag;//是否存在于数据源中
    //Vector<Paper> papers;//这个地方我觉得应该是一个vector保存，方便处理映射,我认为用paperName或者DOI来唯一标识都可以，最后我还是觉得用DOI比较好，因为DOI是唯一的，而且是不变的，而paperName是可以变的，比如有的时候会有一些错误，然后就会改名字，但是DOI是不会变的，所以我觉得用DOI比较好
    public Author(String _authorName,String _orcid,String _authorInstitution){
        authorName = _authorName;
        orcid = _orcid;
        flag = true;//既然被有参构造，肯定是存在于数据源头中吧？
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
}
