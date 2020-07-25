package com.givevision.rochesightchart;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class LearnMachine {
    //represent charts series: random "down", "up", "left", "right" string in array of 5 by series
    private ArrayList<String[]> charts = new ArrayList<>();
    //represent results by eye: chart,series result
    private ArrayList<int[]> results_left = new ArrayList<>();
    private ArrayList<int[]> results_right = new ArrayList<>();
    //represent optotypes series: Visual Angle(min), LogMAR, Approximate M-units, Gap (mm),Outer diameter (mm)
    private ArrayList<float[]> optotypes =new ArrayList<>();

    private static final String MyPREFERENCES = "my preferences";
    private static final String PREF_M = "M-Unit";
    private static final String PREF_RESULT_OF_5 = "result of 5";
    private static SharedPreferences sharedpreferences;
    /**
     *
     */
    public LearnMachine(Context ctx){

        float[] optotype=new float [] {20f,1.3f,80f,23.08f,116.18f};
        optotypes.add(optotype);
        optotype=new float [] {16f,1.2f,63f,18.33f,92.20f};
        optotypes.add(optotype);
        optotype=new float [] {12.5f,1.1f,50f,14.69f,72.24f};
        optotypes.add(optotype);
        optotype=new float [] {10f,1f,40f,11.64f,58.18f};
        optotypes.add(optotype);
        optotype=new float [] {8f,.9f,32f,9.24f,46.21f};
        optotypes.add(optotype);
        optotype=new float [] {6.3f,.8f,25f,7.34f,36.71f};
        optotypes.add(optotype);
        optotype=new float [] {5f,.7f,20f,5.83f,29.16f};
        optotypes.add(optotype);
        optotype=new float [] {4f,.6f,16f,4.63f,23.16f};
        optotypes.add(optotype);
        optotype=new float [] {3.2f,.5f,12.5f,3.68f,18.40f};
        optotypes.add(optotype);
        optotype=new float [] {2.5f,.4f,10f,2.92f,14.61f};
        optotypes.add(optotype);
        optotype=new float [] {2f,.3f,8f,2.34f,11.61f};
        optotypes.add(optotype);
        optotype=new float [] {1.6f,.2f,6.3f,1.84f,9.22f};
        optotypes.add(optotype);
        optotype=new float [] {1.25f,0.1f,5f,1.46f,7.32f};
        optotypes.add(optotype);
        optotype=new float [] {1f,0f,4f,1.6f,5.82f};
        optotypes.add(optotype);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "constructor optotypes done");        }
        Random r = new Random();
        String[] array=new String [] {"down", "up", "left", "right"};
        for(int j=0; j<optotypes.size();j++){
            String[] chart=new String []{"","","","",""};
            for(int i=0; i<chart.length; i++){
                int p = r.nextInt(4);
                boolean exist=false;
                for(int k=0; k<4; k++){
                    if(chart[k].contains(array[p])){
                        exist=true;
                    }
                }
                while(exist && i<chart.length-1){
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
        sharedpreferences  = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "constructor done");        }
    }

    /**
     *
     * @param chart
     * @return lenght
     */
    public int getSizeChartsPos(int chart){
        String [] array=charts.get(chart);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "getSizeChartsPos= "+array.length);        }
        return array.length;
    }

    /**
     *
     * @return sizes
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
            if(eye==0){
                int[] array= results_left.get(chart);
                array[pos]=1;
                results_left.set(chart,array);
                array= results_left.get(chart);
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "left eye chart= "+chart + " said= "+said +" result= "+array[pos]);
                }
            }else{
                int[] array= results_right.get(chart);
                array[pos]=1;
                results_right.set(chart,array);
                array= results_right.get(chart);
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "right eye chart= "+chart + " said= "+said +" result= "+array[eye]);
                }
            }


        }
    }

    /**
     * @param eye
     * @return result
     */
    public boolean isResultOk(int chart ,int eye){
        int result=0;
        if(eye==0){
            int[] array= results_left.get(chart);
            for(int i=0; i<array.length;i++){
                result=result+ array[i];
            }
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_LEARN, "getResult = "+result+ " chart= "+chart +" eye= "+eye);
            }
            if(result>=3){
                return true;
            }
        }else{
            int[] array= results_right.get(chart);
            for(int i=0; i<array.length;i++){
                result=result+ array[i];
            }
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_LEARN, "getResult = "+result+ " chart= "+chart +" eye= "+eye);
            }
            if(result>=3){
                return true;
            }
        }

        return false;
    }

    /**
     * @param eye
     * @return result
     */
    public String getResult(int eye){
        int resultOkPos=-1;
        int total=0;
        //find last good series
        for (int p=0; p<results_left.size(); p++){
            if(isResultOk(p,eye)){
                resultOkPos=p;
            }
        }
        //find next series result
        if(resultOkPos>-1 && resultOkPos<results_left.size()-1){
            float[] array=  optotypes.get(resultOkPos);
            int[]  resultNoArray=  results_left.get(resultOkPos+1);
            for (int p=0; p<resultNoArray.length; p++){
                total=total+resultNoArray[p];
            }
            upDatePref(PREF_M,array[2]);
            upDatePref(PREF_RESULT_OF_5,total);
            if(array[2]-(int)array[2]>0){
                return "4/"+String.format("%.1f", array[2])+" and "+total +" of 5";
            }
            return "4/"+(int)array[2]+" and "+total +" of 5";
        }else if(resultOkPos==results_left.size()-1) {
            upDatePref(PREF_M,4);
            upDatePref(PREF_RESULT_OF_5,0);
            return "4/4";
        }else{
            return "error of reading";
        }
    }

    /**
     * @param
     * @return
     */
    public void clearResult(){
        results_left.clear();
        for(int j=0; j<optotypes.size();j++){
            results_left.add(new int[]{0,0,0,0,0});
        }
        results_right.clear();
        for(int j=0; j<optotypes.size();j++){
            results_right.add(new int[]{0,0,0,0,0});
        }
    }

    /**
     * @param chart
     * @param chartPos
     * @return chartPosition
     */
    public String getChartPosString(int chart, int chartPos) {
        if(charts.size()>chart && charts.get(chart).length>chartPos) {
            return charts.get(chart)[chartPos];
        }
        return "";
    }

    /**
     * @param optotype
     * @param optotypePos
     * @return value of Optotype in position
     */
    public float getOptotype(int optotype,int optotypePos) {
        if(optotypes.size()>optotype && optotypes.get(optotype).length>optotypePos) {
            return optotypes.get(optotype)[optotypePos];
        }
        return -1;
    }

    /**
     * @param optotype
     * @return Outer Diameter
     */
    public float getOptotypeOuterDiameter(int optotype) {
        if(optotypes.size()>optotype) {
            return optotypes.get(optotype)[4];
        }
        return -1;
    }

    protected void upDatePref(String key, String value){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    protected void upDatePref(String key, int value){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    protected void upDatePref(String key, float value){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }
    protected void upDatePref(String key, boolean value){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public SharedPreferences getSharedPreferences(){
        return sharedpreferences;
    }
}