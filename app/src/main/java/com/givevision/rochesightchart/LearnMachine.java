package com.givevision.rochesightchart;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LearnMachine {

    private ArrayList charts = new ArrayList<String[]>();
    private ArrayList results = new ArrayList();
    private int totPos=0;

    /**
     *
     */
    public LearnMachine(){

        charts.add(new String [] {"right", "down"});
        charts.add(new String [] {"up", "right", "left"});
        charts.add(new String [] {"down", "up", "left", "right"});
        charts.add(new String [] {"right","down", "left","down", "up"});
        charts.add(new String [] {"down","up","down", "right","left", "up"});
        results.add(0);
        results.add(0);
        results.add(0);
        results.add(0);
        results.add(0);

        for (int p=0; p<charts.size(); p++){
            String [] array= (String[]) charts.get(p);
            totPos=totPos+array.length;
        }
    }

    /**
     *
     * @param pos
     * @return
     */
    public int getSizeChartsPos(int pos){
        String [] array=(String [])charts.get(pos);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "getSizeChartsPos= "+array.length);        }
        return array.length;
    }

    /**
     *
     * @return
     */
    public int getSizeCharts(){
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "getSizeCharts= "+charts.size());        }
        return charts.size();
    }

    /**
     *
     * @param chart
     * @param pos
     * @param said
     */
    public void setResult(int chart, int pos, String said){         
        String [] arrayChart = (String[]) charts.get(chart);
        if(arrayChart[pos].equals(said)){
            results.add(chart,(int)results.get(chart)+1);
        }
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "chart= "+chart + " said= "+said +" result= "+results.get(chart));
        }
    }

    /**
     *
     * @return
     */
    public int getResult(){

        int result=0;
        int total=0;

        for (int p=0; p<charts.size(); p++){
            String[] array= (String[]) charts.get(p);
            total=total + array.length;
        }

        for (int p=0; p<results.size(); p++){
            result=result+(int)results.get(p);
        }

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "getResult= "+result);        }
        
        return result*100/total;
    }

    public void clearResult(){
        results.clear();
        results.add(0);
        results.add(0);
        results.add(0);
        results.add(0);
        results.add(0);
    }
}