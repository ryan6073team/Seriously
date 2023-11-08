package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Vector;

public class Paper {
    boolean isAlive = false;
    int lifeSpan = 12; //默认保护期一年
    LevelManager.Level level = LevelManager.Level.E;
    Double rankWeight = 1.0; //等级的权值在保护期开始为1;
    String paperName,doi,journal;
    CitingStatusTypes paperStatus;
    Double paperImpact=0.0;//保存文章的影响力
    int  publishedYear=0;//不设置默认值，因为必须有，但是按照表结构似乎不是这样，先假设可以有默认值，方便运行
    int publishedMonth=0;//出版月份，待更新
    Vector<String> citingList,authorIDList,citedList;//新增citedList，待更新,存储doi
    //received accepted revised 已被删除，CitingStatusTypes类实际上已失去作用
    Integer citedTimes=0;//citedList长度


    Vector<Edge> edgeList;
    public Paper(){
        citingList = new Vector<>();
        authorIDList = new Vector<>();
        citedList = new Vector<>();
        edgeList = new Vector<>();
    }
    public void setYear(int _year, CitingStatusTypes _citingstatus/*fileinput待更新，该函数调用应删去此参数*/){
        publishedYear = _year;
    }
    public void setMonth(int _month){//该函数应被fileinput调用以初始化month
        assert(_month>0&&_month<13);//True继续执行 False停止执行
        publishedMonth = _month;
    }
    public Vector<String> getCitingList(){return citingList;}
    public Vector<String> getCitedList(){return citedList;}
    public Vector<String> getAuthorIDList(){return authorIDList;}//这个我觉得可以考虑使用map,有的作者属性就是一作，有的共一作，有的二作，有的是通讯作者，这一篇论文赋予扮演不同角色的作者的影响力也不同

    public CitingStatusTypes getPaperStatus() {
        return paperStatus;
    }
    public String getDoi(){
        return doi;
    }
    public String getJournal(){return journal;}
    public void setJournal(String journal){this.journal = journal;}
    public void setCitedTimes(int inDegree) {
        citedTimes = inDegree;
    }
    public int getPublishedYear() {
        return publishedYear;
    }
    public int getPublishedMonth(){return publishedMonth;}
    public Double getPaperImpact(){return paperImpact;}
    public void setPaperImpact(Double _paperImpact){paperImpact = _paperImpact;}
    public Vector<Edge> getEdgeList(){
        return edgeList;
    }
    public boolean getIsAlive(){
        return isAlive;
    }
    public void setIsAlive(boolean flag){
        isAlive = flag;
    }//用于更新论文存活状态，具体实现后续补充。
    public LevelManager.Level getLevel(){
        return level;
    }
    public void setLevel(LevelManager.Level l){
        level = l;
    }
    public int getLifeSpan(){
        return lifeSpan;
    }
    public void setLifeSpan(int life){
        lifeSpan = life;
    }
    public double getRankWeight(){
        return rankWeight;
    }
    public void setRankWeight(double weight){
        rankWeight = weight;
    }
}
