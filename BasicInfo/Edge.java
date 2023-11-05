package com.github.ryan6073.Seriously.BasicInfo;

import static com.github.ryan6073.Seriously.BasicInfo.CitingStatusTypes.*;

public class Edge {
    Double citingKey= 0.0;
    CitingStatusTypes citingStatus;
    String doi;
    int year = 0;
    int month = 0;

    public Edge(double _citingKey,int _citingStatus,int _year,String _doi ){
        citingKey = _citingKey;
        citingStatus = choiceTypes(_citingStatus);
        year = _year;
        doi = _doi;
        month = DataGatherManager.getInstance().dicDoiPaper.get(_doi).publishedMonth;
    }
    public Double getCitingKey(){return citingKey;}
    public CitingStatusTypes getCitingStatus(){return citingStatus;}
}

