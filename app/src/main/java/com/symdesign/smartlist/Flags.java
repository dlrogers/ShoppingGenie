package com.symdesign.smartlist;

/**
 * Created by dennis on 9/13/16.
 */
public class Flags {
    int flgs;

    public Flags(int flags){
        flgs = flags;
    }

    boolean inList() {
        return (flgs&1)>0;
    }
    boolean deleted() {
        return (flgs&2)>0;
    }
    boolean changed() {
        return (flgs&4)>0;
    }

    void setList() {
        flgs = flgs | 1 ;
    }

    void setChg() {
        flgs = flgs | 2 ;
    }
}
