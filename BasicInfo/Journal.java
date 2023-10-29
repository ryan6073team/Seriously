package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Random;
import java.util.Vector;

public class Journal implements Comparable<Journal>{
    String journalName;
    Vector<String> journalPapers;//用DOI唯一标识
    Double journalImpact=0.0;//不知道计算方法
    Double IF;
    Integer rank; //  用1、2、3、4划分为四个等级
    public Journal(){journalPapers = new Vector<>();}
    public Double getJournalImpact(){return journalImpact;}
    public void setJournalImpact(Double _journalImpact){journalImpact = _journalImpact;}
    public Vector<String> getJournalPapers(){return journalPapers;}

    public void setIF(Double IF) {
        Random random = new Random();
        this.IF = (double) (random.nextInt(100) + 1);
    } //  需要修改
    public Double getIF() {
        return IF;
    }
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    public Integer getRank(){
        return rank;
    }

    @Override
    public int compareTo(Journal o) {
        // 按 double 类型属性降序排序
        if (this.IF > o.IF) {
            return -1; // 返回负数表示降序
        } else if (this.IF < o.IF) {
            return 1; // 返回正数表示降序
        } else {
            return 0; // 相等时返回0
        }
    }
}
