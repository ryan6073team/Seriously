package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Vector;

public class Journal {//
    String journalName;
    Vector<String> journalPapers;//用DOI唯一标识
    Double journalImpact=0.0;//不知道计算方法
    public Journal(){
        journalPapers = new Vector<>();

    }
}
