package com.symdesign.smartlist;

/**
 * Created by dennis on 8/20/16.
 */
public class Params {
    String _nm;
    int _il;
    long _lt,_la,_id;
    double _r;
    static final Params newParams = new Params("",1,MainActivity.getTime(),3*MainActivity.day,0,0);

    public Params(String nm,int il,long lt,long la,double r,long id){
        _nm=nm;_il=il;_lt=lt;_la=la;_r=r;_id=id;
    }
}
