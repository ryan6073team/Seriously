package com.github.ryan6073.Seriously.BasicInfo;

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
    public double getAuthorPaperImpact(LevelManager.Level authorLevel, LevelManager.Level paperLevel){
        //含义为一位authorlevel的作者发表或删除一篇paperlevel的论文将会对该作者的影响力产生多大的影响
        return authorPaperForm[authorLevel.getIndex()][paperLevel.getIndex()];
    }
}
