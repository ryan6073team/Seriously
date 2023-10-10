package com.github.ryan6073.Seriously.BasicInfo;

public enum CitingStatusTypes {
    PUBLISHED,
    RECEIVED,
    ACCEPTED,//
    REVISED;
    static int totalCount( ) { return values().length; }
    static CitingStatusTypes choiceTypes(int _citingStatus){
        switch (_citingStatus) {
            case 1 -> {
                return PUBLISHED;
            }
            case 2 -> {
                return RECEIVED;
            }
            case 3 -> {
                return ACCEPTED;
            }
            case 4 -> {
                return REVISED;
            }
        }
        System.out.println("Type choose error!");
        return null;
    }
}
