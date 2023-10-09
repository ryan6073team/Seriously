package com.github.ryan6073.Seriously.src.BasicInfo;

import java.util.Vector;

public class Institution {
    String institutionName;
    Vector<String> institutionAuthors;//用orcid唯一标识,这里不应该是papers
    Double institutionImpact=0.0;//不知道计算方法
    public Institution(){
        institutionAuthors = new Vector<>();
    }
}
