package com.github.ryan6073.Seriously.BasicInfo;

import com.github.ryan6073.Seriously.Coefficient.CoefficientStrategy;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class Paper {
    boolean isAlive = false;
    int life = 0; //默认保护期一年
    LevelManager.Level level = LevelManager.Level.E;
    LevelManager.CitationLevel citationLevel = LevelManager.CitationLevel.LOW;
    Double rankWeight = 1.0; //等级的权值在保护期开始为1;
    String paperName,doi,journal;
    Double paperImpact=0.0;//保存文章的影响力
    int  publishedYear=0;//不设置默认值，因为必须有，但是按照表结构似乎不是这样，先假设可以有默认值，方便运行
    int publishedMonth=0;//出版月份，待更新
    int isRead = 0;//0未读 1已读
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
    public void setYear(int _year/*fileinput待更新，该函数调用应删去此参数*/){
        publishedYear = _year;
    }
    public void setMonth(int _month){//该函数应被fileinput调用以初始化month
        assert(_month>0&&_month<13);//True继续执行 False停止执行
        publishedMonth = _month;
    }
    public Vector<String> getCitingList(){return citingList;}
    public Vector<String> getCitedList(){return citedList;}
    public Vector<String> getAuthorIDList(){return authorIDList;}//！！！！！！！这个我觉得可以考虑使用map,有的作者属性就是一作，有的共一作，有的二作，有的是通讯作者，这一篇论文赋予扮演不同角色的作者的影响力也不同

    public String getDoi(){
        return doi;
    }
    public String getPaperName(){
        return paperName;
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
    public LevelManager.CitationLevel getCitationLevel(){
        CoefficientStrategy coefficientStrategy = new CoefficientStrategy();
        return coefficientStrategy.getCitationLevel(this);
    }

    public void setLevel(LevelManager.Level l){
        level = l;
    }
    public int getLife(){
        return life;
    }
    public void setLife(int _life){life = _life;}
    public double getRankWeight(){
        return rankWeight;
    }
    public void setRankWeight(double weight){
        rankWeight = weight;
    }
    public int getIsRead() {
        return isRead;
    }
    public void setIsRead(int ifRead){
        this.isRead = ifRead;
    }
    // 定义一个getAgeGroup方法，用于获取论文的年龄组
    public LevelManager.PaperAgeGroup getAgeGroup() {
        // 获取论文的诞生时长，以月为单位
        int lifespan = this.getLife();
        // 根据论文的诞生时长，返回相应的年龄组枚举值
        if(lifespan > 0 && lifespan <= 4){
            // 如果论文的诞生时长在1-4月之间，属于青年期
            return LevelManager.PaperAgeGroup.CHILD;
        }else if(lifespan > 4 && lifespan <= 8){
            // 如果论文的诞生时长在5-8月之间，属于壮年期
            return LevelManager.PaperAgeGroup.YOUNG;
        }else if(lifespan > 8 && lifespan <= 12){
            // 如果论文的诞生时长在9-12月之间，属于老年期
            return LevelManager.PaperAgeGroup.OLD;
        }else if(lifespan == 13){
            // 如果论文的诞生时长为13，表示论文已经超过一年，属于成熟期
            return LevelManager.PaperAgeGroup.MATURE;
        }else{
            // 如果论文的诞生时长不在上述范围内，返回null
            return null;
        }
    }

    //定义一个方法getMonthsBetween，接受两个Date类型的参数，返回一个int类型的结果
    public int getMonthsBetween(Date date1, Date date2) {
        // 创建两个Calendar对象，分别用来存储date1和date2的年份和月份
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        // 设置cal1和cal2的时间为date1和date2
        cal1.setTime(date1);
        cal2.setTime(date2);
        // 获取cal1和cal2的年份和月份
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH);
        int month2 = cal2.get(Calendar.MONTH);
        // 计算两个日期之间相差的月份数，注意要考虑年份的差异
        int months = (year2 - year1) * 12 + (month2 - month1);
        // 返回月份数的绝对值，因为不考虑日期的先后顺序
        return Math.abs(months);
    }

}
