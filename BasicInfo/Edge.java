package com.github.ryan6073.Seriously.BasicInfo;

public class Edge {
    Double citingKey= 0.0;
    String doi;
    int year = 0;
    int month = 0;

    public Edge(double _citingKey,int _year,String _doi ){
        citingKey = _citingKey;
        year = _year;
        doi = _doi;
        month = DataGatherManager.getInstance().dicDoiPaper.get(_doi).publishedMonth;
    }
    public Double getCitingKey(){return citingKey;}
    public String getDoi(){
        return doi;
    }
}

