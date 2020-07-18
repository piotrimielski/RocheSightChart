package com.givevision.rochesightchart;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class LearnMachine {

    private ArrayList<String[]> charts = new ArrayList<>();
    private ArrayList<int[]> results = new ArrayList<>();

    /**
     *
     */
    public LearnMachine(){
        Random r = new Random();
        String[] array=new String [] {"down", "up", "left", "right"};
        for(int j=0; j<5;j++){
            String[] chart=new String []{"","","",""};
            for(int i=0; i<chart.length; i++){
                int p = r.nextInt(4);
                boolean exist=false;
                for(int k=0; k<4; k++){
                    if(chart[k].contains(array[p])){
                        exist=true;
                    }
                }
                while(exist){
                    p = r.nextInt(4);
                    exist=false;
                    for(int k=0; k<4; k++){
                        if(chart[k].contains(array[p])){
                            exist=true;
                        }
                    }
                }
                chart[i]=array[p];
            }
            charts.add(j,chart);
        }
        for(int j=0; j<5;j++){
            String[] chart=charts.get(j);
            Log.i(Util.LOG_TAG_LEARN, "chart= "+j);
            for(int i=0; i<4; i++){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "chart= value= "+chart[i]);        }
            }

        }
    }

    /**
     *
     * @param chart
     * @return
     */
    public int getSizeChartsPos(int chart){
        String [] array=charts.get(chart);
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

    public String getChart(int chart, int chartPos) {
        if(charts.size()>chart && charts.get(chart).length>chartPos) {
            return charts.get(chart)[chartPos];
        }
        return "";
    }
}