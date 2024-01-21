package com.github.ryan6073.Seriously.BasicInfo;

import java.util.Map;

import static com.github.ryan6073.Seriously.BasicInfo.Journal.levelImpact;

public class LevelManager {
    public enum Level {
        A(0),
        B(1),
        C(2),
        D(3),
        E(4);
        private int index;
        public static int levelNum = 5;
        Level(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
        public static Level getLevelByIndex(int index){
            switch (index) {
                case 0 -> {
                    return Level.A;
                }
                case 1 -> {
                    return Level.B;
                }
                case 2 -> {
                    return Level.C;
                }
                case 3 -> {
                    return Level.D;
                }
                default -> {
                    return Level.E;
                }
            }
        }
    }

    public enum TimeState{
        PRE(0),
        MIDDLE(1),
        LATE(2);
        private int index;
        public static int stateNum = 3;
        TimeState(int index){this.index = index;}
        public int getIndex(){return index;}
        public static TimeState getTimeStateByIndex(int index){
            switch (index){
                case 0 -> {return PRE;}
                case 1 -> {return MIDDLE;}
                case 2 -> {return LATE;}
                default -> {return null;}
            }
        }
    }

    public enum PaperAgeGroup{
        CHILD(0),
        YOUNG(1),
        OLD(2),
        MATURE(3);
        private int index;
        public static int ageGroupNum = 4;
        PaperAgeGroup(int index){this.index = index;}
        public int getIndex(){return index;}
        public static PaperAgeGroup getPaperAgeGroupByIndex(int index){
            switch (index){
                case 0 -> {return CHILD;}
                case 1 -> {return YOUNG;}
                case 2 -> {return OLD;}
                case 3->{return MATURE;}
                default -> {return null;}
            }
        }
    }

    public enum CitationLevel {
        HIGH(0),
        MEDIUM(1),
        LOW(2);
        private int index;
        public static int citationLevelNum = 3;
        CitationLevel(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static CitationLevel getCitationLevelByIndex(int index) {
            switch (index) {
                case 0 -> {
                    return HIGH;
                }
                case 1 -> {
                    return MEDIUM;
                }
                case 2 -> {
                    return LOW;
                }
                default -> {
                    return null;
                }
            }
        }
    }
}
