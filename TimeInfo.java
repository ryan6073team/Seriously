package com.github.ryan6073.Seriously;

import java.util.Objects;

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
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TimeInfo timeInfo = (TimeInfo) obj;
        return year == timeInfo.year && month == timeInfo.month;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month);
    }

}

