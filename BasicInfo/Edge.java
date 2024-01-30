package com.github.ryan6073.Seriously.BasicInfo;


public class Edge {
    Double citingKey= 0.0;
    String doi;
    int year = 0;
    int month = 0;
    String citingDoi;

    public Edge(double _citingKey,int _year,String _doi,String _citingDoi){
        citingKey = _citingKey;
        year = _year;
        doi = _doi;
        month = DataGatherManager.getInstance().dicDoiPaper.get(_doi).publishedMonth;
        citingDoi = _citingDoi;
    }
    public Edge(double _citingKey,int _year,String _doi, int _month ){
        citingKey = _citingKey;
        year = _year;
        doi = _doi;
        month = _month;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Edge)) {
            return false;
        }
        Edge product = (Edge) o;
        return (this.doi.compareTo(product.doi)==0) && (this.citingDoi.compareTo(product.citingDoi)==0);
    }

    public Double getCitingKey(){return citingKey;}
    public String getDoi(){
        return doi;
    }
    public String getCitingDoi(){return citingDoi;}
    public int getYear(){return year;}
    public int getMonth() {return month;}
}

