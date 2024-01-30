package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class Journal implements Comparable<Journal>{
    public static Map<LevelManager.Level,Double> levelImpact;
    public Vector<String> journalPapers;//用DOI唯一标识
    private String journalName;
    private Double journalImpact=0.0;
    private Double IF;
    private int ifExist = 0;//0不存在1存在
    private LevelManager.Level level; //  A,B,C,D,E 一共5个等级
    public Journal(){journalPapers = new Vector<>();}
    public void setIfExist(int ifExist){this.ifExist = ifExist;}
    public int getIfExist(){return ifExist;}
    public static void updateLevelImpact(){
        int[] levelNum = new int[101];
        for(int i=0;i<101;i++)
            levelNum[i]=0;
        for(Journal journal:DataGatherManager.getInstance().journals){
            levelNum[journal.level.getIndex()]++;
            if (!levelImpact.containsKey(journal.level))
                levelImpact.put(journal.level, journal.getJournalImpact());
            else {
                double impact = levelImpact.get(journal.level) * (levelNum[journal.level.getIndex()] - 1);
                impact += journal.getJournalImpact();
                impact = impact / levelNum[journal.level.getIndex()];
                levelImpact.put(journal.level, impact);
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
    public void setLevel(LevelManager.Level level) {
        this.level = level;
    }
    public LevelManager.Level getLevel(){
        return level;
    }
    @Override
    public int compareTo(Journal o) {
        // 按 double 类型属性降序排序
        if (this.journalImpact > o.journalImpact) {
            return -1; // 返回负数表示降序
        } else if (this.journalImpact < o.journalImpact) {
            return 1; // 小于返回正数表示降序
        } else {
            return 0; // 相等时返回0
        }
    }
}
