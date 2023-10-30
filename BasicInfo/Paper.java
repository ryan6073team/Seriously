package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Vector;

public class Paper {
    String paperName,doi;
    CitingStatusTypes paperStatus;
    Double paperImpact=0.0;//保存文章的影响力
    int  publishedYear=0;//不设置默认值，因为必须有，但是按照表结构似乎不是这样，先假设可以有默认值，方便运行
    int publishedMonth=0;//出版月份，待更新
    Vector<String> citingList,authorIDList,citedList;//新增citedList，待更新
    //received accepted revised 已被删除，CitingStatusTypes类实际上已失去作用
    Integer citedTimes=0;//citedList长度
    Vector<String> journals;
    //这个地方应该不存在一篇文章多个期刊的情况，埋个坑；他可能会有发表在多种刊物上的情况出现，然后现在也不知道到底就是会不会有这种特殊情况。然后你如果是用一个string的话，它就无法容错，但是如果你是用vector的话，它就有容错的空间了，它是一个兼容的情况啊，如果它只有一个期刊，那就只有一个元素。那如果有多个期刊的话，那我这个vector也可以直接就是把它兼容下来，但是如果遇见那种有多种期刊的情况，如果只有一个string的话，它这个代码就不能处理呀，就要重新改呀，那就会更麻烦一些。
    public Paper(){
        citingList = new Vector<>();
        authorIDList = new Vector<>();
        journals = new Vector<>();
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

    public void setCitedTimes(int inDegree) {
        citedTimes = inDegree;
    }

    public int getPublishedYear() {
        return publishedYear;
    }
    public int getPublishedMonth(){return publishedMonth;}
    public Double getPaperImpact(){return paperImpact;}
    public void setPaperImpact(Double _paperImpact){paperImpact = _paperImpact;}
}
