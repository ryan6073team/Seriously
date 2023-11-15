package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class Journal implements Comparable<Journal>{
    public static Map<LevelManager.Level,Double> levelImpact;
    public Vector<String> journalPapers;//用DOI唯一标识
    public String journalName;
    private Double journalImpact=0.0;
    private Double IF;
    private Integer rank; //  用1、2、3、4划分为四个等级
    public Journal(){journalPapers = new Vector<>();}
    public static void updateLevelImpact(){
        int[] levelNum = new int[101];
        for(int i=0;i<101;i++)
            levelNum[i]=0;
        for(Journal jounal:DataGatherManager.getInstance().journals){
            levelNum[jounal.rank]++;
            if (levelImpact.containsKey(LevelManager.RanktoLevel(jounal.rank)))
                levelImpact.put(LevelManager.RanktoLevel(jounal.rank), jounal.getJournalImpact());
            else {
                double impact = levelImpact.get(LevelManager.RanktoLevel(jounal.rank)) * (levelNum[jounal.rank] - 1);
                impact += jounal.getJournalImpact();
                impact = impact / levelNum[jounal.rank];
                levelImpact.put(LevelManager.RanktoLevel(jounal.rank), impact);
            }
        }
    }
    public String getJournalName(){
        return journalName;
    }
    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    public Double getJournalImpact(){return journalImpact;}
    public void setJournalImpact(Double _journalImpact){journalImpact = _journalImpact;}
    public Vector<String> getJournalPapers(){return journalPapers;}

    public void setIF(DataGatherManager dataGatherManager) {
        Random random = new Random();
        this.IF = (double) (random.nextInt(100) + 1);
        //this.IF = dataGatherManager.dicJournalIF.get(journalName);
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
        if (this.journalImpact > o.journalImpact) {
            return -1; // 返回负数表示降序
        } else if (this.journalImpact < o.journalImpact) {
            return 1; // 返回正数表示降序
        } else {
            return 0; // 相等时返回0
        }
    }
}
