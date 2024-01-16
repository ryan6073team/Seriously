package com.github.ryan6073.Seriously.BasicInfo;

import com.github.ryan6073.Seriously.Graph.GraphManager;
import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//ImpactForm用于记录不同等级的作者和不同等级的论文之间的影响关系
public class ImpactForm {
    //[5][5]代表作者和论文等级暂定为ABCDE
    private double [][] authorPaperForm = new double[5][5];
    private ImpactForm(){
        //调用构造函数时对Form数组进行初始化
        for(int i=0;i<5;i++)
            for(int j=0;j<5;j++)
                authorPaperForm[i][j]=0.0;
        //********得到该数组的代码未定**************//
    }
    private static ImpactForm mImpactForm = new ImpactForm();
    public static ImpactForm getInstance(){return mImpactForm;}
    //更新影响关系表
    public void updateForm(){}

    public double cal_impact(Author author,Paper paper){//用于计算一篇处于特定等级的论文对一个特定等级作者的影响

        return 0;
    }
    public double getAuthorPaperImpact(LevelManager.Level authorLevel, LevelManager.Level paperLevel){
        //含义为一位authorlevel的作者发表或删除一篇paperlevel的论文将会对该作者的影响力产生多大的影响
        Vector<Paper> curruntpapers=DataGatherManager.getInstance().papers;
        Vector<Author> authors=new Vector<>();
        Vector<Paper> papers=new Vector<>();
        List<Double> impacts = new ArrayList<>();
        //获取特定等级作者集合
        for (Author author : DataGatherManager.getInstance().dicOrcidAuthor.values()) {
            if (author.getLevel().equals(authorLevel)) {
                authors.add(author);
            }
        }
        //获取特定等级的论文
        for(Author author : authors){
            Vector<Paper> papers1=DataGatherManager.getInstance().dicAuthorPaper.get(author);
            for(Paper paper:papers1){
                if(paper.getLevel().equals(paperLevel)){
                    double impact=cal_impact(author,paper);
                    impacts.add(impact);
                    papers.add(paper);
                }
            }
        }
        double sum=0;
        for(double data:impacts){
            sum+=data;
        }



        return sum/impacts.size();
    }
    public void init_authorPaperForm(){
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                authorPaperForm[i][j]=getAuthorPaperImpact(LevelManager.Level.getLevelByIndex(i),LevelManager.Level.getLevelByIndex(j));
            }
        }
    }

}


