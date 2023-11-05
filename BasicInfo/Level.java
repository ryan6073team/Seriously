package com.github.ryan6073.Seriously.BasicInfo;

public enum Level {
    A(0),
    B(1),
    C(2),
    D(3);
    private int index;
    Level(int index){this.index = index;}
    public int getIndex(){return index;}
}
