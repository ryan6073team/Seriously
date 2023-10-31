package com.github.ryan6073.Seriously.BasicInfo;

import static com.github.ryan6073.Seriously.BasicInfo.CitingStatusTypes.*;

public class Edge {
    Double citingKey= 0.0;
    CitingStatusTypes citingStatus;

    int year = 0;

    public Edge(double _citingKey,int _citingStatus,int _year ){
        citingKey = _citingKey;
        citingStatus = choiceTypes(_citingStatus);
        year = _year;
    }
    public Double getCitingKey(){return citingKey;}
    public CitingStatusTypes getCitingStatus(){return citingStatus;}
}

