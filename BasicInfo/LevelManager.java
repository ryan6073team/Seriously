package com.github.ryan6073.Seriously.BasicInfo;

import static com.github.ryan6073.Seriously.BasicInfo.Journal.levelImpact;

public class LevelManager {
    public enum Level {
        A(0),
        B(1),
        C(2),
        D(3),
        E(4);
        private int index;

        Level(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
    public static Level RanktoLevel(int rank){
        switch (rank) {
            case 1 -> {
                return Level.A;
            }
            case 2 -> {
                return Level.B;
            }
            case 3 -> {
                return Level.C;
            }
            case 4 -> {
                return Level.D;
            }
            default -> {
                return Level.E;
            }
        }
    }

}