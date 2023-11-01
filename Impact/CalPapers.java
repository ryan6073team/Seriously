package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.BasicInfo.Paper;
import com.github.ryan6073.Seriously.TimeInfo;

import java.util.Vector;

public class CalPapers {
    private Vector<Paper> thenPapers;//当时的论文集合，注意是引用不是拷贝
    public CalPapers(Vector<String> thenPaperDois){
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        for(String doi:thenPaperDois){
            thenPapers.add(dataGatherManager.dicDoiPaper.get(doi));//java中容器Vectort用add添加对象是对象引用
        }
    }
    public void excute(){
        updatePapersImpact();
    }
    public void updatePapersImpact(){

    }
}
