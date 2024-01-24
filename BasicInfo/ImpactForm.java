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
    private double [][][] authorPaperForm = new double[5][5][3];
//    private ImpactForm(){
//        //调用构造函数时对Form数组进行初始化
//        for(int i=0;i<5;i++)
//            for(int j=0;j<5;j++)
//                authorPaperForm[i][j]=0.0;
//        //********得到该数组的代码未定**************//
//    }
    private static ImpactForm mImpactForm = new ImpactForm();
    public static ImpactForm getInstance(){return mImpactForm;}
    //更新影响关系表
    public double [][][] getForm(){return authorPaperForm;}
    private ImpactForm(){
        for (int i=0;i<LevelManager.Level.levelNum;i++)
            for(int j=0;j< LevelManager.Level.levelNum;j++)
                for(int k=0;k< LevelManager.CitationLevel.citationLevelNum;k++)
                    authorPaperForm[i][j][k]=0.0;
    }

    public void cal_impact(){//用于计算一篇处于特定等级的论文对一个特定等级作者的影响
        authorPaperForm = GraphManager.getInstance().calAllPaperImp(DataGatherManager.getInstance(), GraphManager.getInstance().getMatureGraph());
        System.out.println("Form更新");
    }
    public double getAuthorPaperImpact(LevelManager.Level authorLevel, LevelManager.Level paperLevel, LevelManager.CitationLevel citationLevel){
        return authorPaperForm[authorLevel.getIndex()][paperLevel.getIndex()][citationLevel.getIndex()];
    }
//    public void init_authorPaperForm(){
//        for(int i=0;i<5;i++){
//            for(int j=0;j<5;j++){
//                authorPaperForm[i][j]=getAuthorPaperImpact(LevelManager.Level.getLevelByIndex(i),LevelManager.Level.getLevelByIndex(j));
//            }
//        }
//    }

}


