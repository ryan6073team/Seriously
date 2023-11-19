package com.github.ryan6073.Seriously;

public class TimeInfo implements Comparable<TimeInfo>{
    public int year=0;
    public int month=0;
    public TimeInfo(int _year, int _month){year = _year; month = _month;}
    @Override
    public int compareTo(TimeInfo o){
        if(this.year<o.year)
            return -1;
        else if(this.year>o.year)
            return 1;
        else if(this.month<o.month)
            return -1;
        else if(this.month>o.month)
            return 1;
        else return 0;
    }
}

