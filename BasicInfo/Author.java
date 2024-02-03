package com.github.ryan6073.Seriously.BasicInfo;

import java.util.List;
import java.util.Vector;

public class Author implements Comparable<Author>{
    LevelManager.Level level= LevelManager.Level.E; //初始默认为E等级
    String authorName,orcid;
    int ifExist = 0;//0 代表未出现 1 代表已出现
    Vector<String> authorInstitution;
    Double authorImpact = 0.0;
    boolean flag;//是否存在于数据源中
    public Author(String _authorName,String _orcid){
        authorName = _authorName;
        orcid = _orcid;
        flag = true;//既然被有参构造，肯定是存在于数据源头中吧？
        authorInstitution = new Vector<>();
    }

    //我觉得需要一个papers向量数据成员，来存放一个作者的文章集合
    public int getIfExist(){
        return ifExist;
    }
    public void setIfExist(int ifExist){
        this.ifExist = ifExist;
    }
    public boolean getFlag(){return flag;}
    public String getOrcid(){return orcid;}
    public String getAuthorName(){return authorName;}
    public Double getAuthorImpact(){return authorImpact;}
    public void setAuthorImpact(Double _authorImpact){authorImpact = _authorImpact;}
    public void setLevel(LevelManager.Level l){
        level = l;
    }
    public LevelManager.Level getLevel(){
        return level;
    }
    public Vector<String> getAuthorInstitutions(){
        return authorInstitution;
    }
    public void addAuthorInstitution(String ins){
            authorInstitution.add(ins);
    }
    @Override
    public int compareTo(Author o) {
        // 按 double 类型属性降序排序
        if (this.authorImpact > o.authorImpact) {
            return -1; // 返回负数表示降序
        } else if (this.authorImpact < o.authorImpact) {
            return 1; // 返回正数表示降序
        } else {
            return 0; // 相等时返回0
        }
    }
}
