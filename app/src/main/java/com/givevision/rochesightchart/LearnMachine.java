package com.givevision.rochesightchart;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LearnMachine {

    private ArrayList<String[]> charts = new ArrayList<>();
    private ArrayList<int[]> results = new ArrayList<>();
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
        results.add(new int[]{0,0});
        results.add(new int[]{0,0});
        results.add(new int[]{0,0});
        results.add(new int[]{0,0});
        results.add(new int[]{0,0});

        for (int p=0; p<charts.size(); p++){
            String [] array= (String[]) charts.get(p);
            totPos=totPos+array.length;
        }
    }

    /**
     *
     * @param chart
     * @return
     */
    public int getSizeChartsPos(int chart){
        String [] array=(String [])charts.get(chart);
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
     * @param eye
     */
    public void setResult(int chart, int pos, String said, int eye){
        String [] arrayChart = (String[]) charts.get(chart);
        if(arrayChart[pos].equals(said)){
            int[] array= (int[])results.get(chart);
            array[eye]=array[eye]+1;
            results.set(chart,array);
            array= (int[]) results.get(chart);
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_LEARN, "chart= "+chart + " said= "+said +" result= "+array[eye]);
            }
        }
    }

    /**
     * @param eye
     * @return
     */
    public int getResult(int eye){

        int result=0;
        int total=0;

        for (int p=0; p<charts.size(); p++){
            String[] array= (String[]) charts.get(p);
            total=total + array.length;
        }

        for (int p=0; p<results.size(); p++){
            int[] array= (int[])results.get(p);
            result=result+array[eye];
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_LEARN, "getResult array[eye]= "+array[eye]+ " pos= "+p +" eye= "+eye);
            }
        }

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "getResult= "+result+ " total= "+total);
        }
        
        return result*100/total;
    }

    public void clearResult(){
        results.clear();
        results.add(new int[]{0,0});
        results.add(new int[]{0,0});
        results.add(new int[]{0,0});
        results.add(new int[]{0,0});
        results.add(new int[]{0,0});
    }
}