package com.givevision.rochesightchart;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 */
public class LearnMachine {
    //represent charts series: random "down", "up", "left", "right" string in array of 4 by series
    private ArrayList<String[]> charts = new ArrayList<>();
    //represent results by eye: chart,series result
    private ArrayList<int[]> results_left = new ArrayList<>();
    private ArrayList<int[]> results_right = new ArrayList<>();
    //represent optotypes series: Visual Angle(min), LogMAR, Approximate M-units, Gap (mm),Outer diameter (mm)
    private ArrayList<float[]> optotypes =new ArrayList<>();
    private static final int NBR_OF_CHARACTERS=4;
    private Context context;

    /**
     *
     */
    public LearnMachine(Context ctx){
        context=ctx;
    //Visual Angle(min), LogMAR, Approximate M-units, Gap (mm), Image Outer diameter (mm)
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
            String[] chart=new String []{"","","",""};
            for(int i=0; i<NBR_OF_CHARACTERS; i++) {
                int p = r.nextInt(4);
                chart[i] = array[p];
            }
            charts.add(j, chart);
        }
        for(int j=0; j<optotypes.size();j++){
            String[] chart=charts.get(j);
            Log.i(Util.LOG_TAG_LEARN, "chart= "+j);
            for(int i=0; i<NBR_OF_CHARACTERS; i++){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "serie= "+j+" chart= "+i+" value= "+chart[i]);        }
            }

        }


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
     * @param chart
     * @param pos
     * @param said
     * @param eye
     * @return result
     */
    public int setResult(int chart, int pos, String said, int eye){
        String [] arrayChart = (String[]) charts.get(chart);
        int result;
        if(arrayChart[pos].equals(said)){
            if(eye==0){
                int[] array= results_left.get(chart);
                array[pos]=1;
                results_left.set(chart,array);
                array= results_left.get(chart);
                result=array[pos];
            }else if(eye==1){
                int[] array= results_right.get(chart);
                array[pos]=1;
                results_right.set(chart,array);
                array= results_right.get(chart);
                result=array[pos];
            }else{
                result=-1;
            }
        }else{
            if(eye==0) {
                int[] array = results_left.get(chart);
                result=array[pos];
            }else if(eye==1){
                int[] array= results_right.get(chart);
                result=array[pos];
            }else{
                result=-1;
            }
        }
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "eye= "+eye+" chart= "+chart + " pos= "+pos+
                    " value= "+arrayChart[pos]+" said= "+said +" result= "+result);
        }
        return result;
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

        //find next series result
        if(eye==0){
            //find last good series
            for (int p=0; p<results_left.size(); p++){
                if(isResultOk(p,eye)){
                    resultOkPos=p;
                }
            }
            if(resultOkPos>-1 && resultOkPos<results_left.size()-1){
                float[] array=  optotypes.get(resultOkPos);
                int[]  resultNoArray=  results_left.get(resultOkPos+1);
                for (int p=0; p<resultNoArray.length; p++){
                    total=total+resultNoArray[p];
                }
                Util.upDatePref(context, Util.PREF_M_LEFT,array[2]);
                Util.upDatePref(context, Util.PREF_RESULT_OF_4_LEFT,total);
                if(array[2]-(int)array[2]>0){
                    return String.format("%.1f", array[2]);
//                return "4/"+String.format("%.1f", array[2])+" and "+total +" of 4";
                }
//            return "4/"+(int)array[2]+" and "+total +" of 5";
                return String.format("%.0f", array[2]);
            }else if(resultOkPos==results_left.size()-1) {
                Util.upDatePref(context, Util.PREF_M_LEFT,4);
                Util.upDatePref(context,Util.PREF_RESULT_OF_4_LEFT,0);
                return "4";
            }else{
                return "no reading";
            }
        }else{
            //find last good series
            for (int p=0; p<results_right.size(); p++){
                if(isResultOk(p,eye)){
                    resultOkPos=p;
                }
            }
            if(resultOkPos>-1 && resultOkPos<results_right.size()-1){
                float[] array=  optotypes.get(resultOkPos);
                int[]  resultNoArray=  results_right.get(resultOkPos+1);
                for (int p=0; p<resultNoArray.length; p++){
                    total=total+resultNoArray[p];
                }
                Util.upDatePref(context, Util.PREF_M_RIGHT,array[2]);
                Util.upDatePref(context, Util.PREF_RESULT_OF_4_RIGHT,total);
                if(array[2]-(int)array[2]>0){
                    return String.format("%.1f", array[2]);
//                return "4/"+String.format("%.1f", array[2])+" and "+total +" of 4";
                }
//            return "4/"+(int)array[2]+" and "+total +" of 4";
                return String.format("%.0f", array[2]);
            }else if(resultOkPos==results_right.size()-1) {
                Util.upDatePref(context, Util.PREF_M_RIGHT,4);
                Util.upDatePref(context, Util.PREF_RESULT_OF_4_RIGHT,0);
                return "4";
            }else{
                return "no reading";
            }
        }
    }
    /**
     * @param eye
     * @return result
     */
    public String getEyeResult(int eye){
        int resultOkPos=-1;
        if(eye==0){
            //find last good series
            for (int p=0; p<results_left.size(); p++){
                if(isResultOk(p,eye)){
                    resultOkPos=p;
                }
            }
            if(resultOkPos>-1 && resultOkPos<results_left.size()-1){
                float[] array=  optotypes.get(resultOkPos);
                if(array[2]-(int)array[2]>0){
                    return String.format("%.1f", array[2]);
                }
                return String.valueOf(array[2]);
            }else{
                return "0";
            }
        }else if(eye==1){
            //find last good series
            for (int p=0; p<results_right.size(); p++){
                if(isResultOk(p,eye)){
                    resultOkPos=p;
                }
            }
            if(resultOkPos>-1 && resultOkPos<results_right.size()){
                float[] array=  optotypes.get(resultOkPos);
                if(array[2]-(int)array[2]>0){
                    return String.format("%.1f", array[2]);
                }
                return String.valueOf(array[2]);
            }else{
                return "0";
            }
        }else{
            return "0";
        }
    }

    /**
     * @param
     * @return
     */
    public void clearResult(){
        results_left.clear();
        for(int j=0; j<optotypes.size();j++){
            results_left.add(new int[]{0, 0, 0, 0});
        }
        results_right.clear();
        for(int j=0; j<optotypes.size();j++){
            results_right.add(new int[]{0,0,0,0});
        }
        Util.upDatePref(context, Util.PREF_M_LEFT,0);
        Util.upDatePref(context, Util.PREF_M_RIGHT,0);
        Util.upDatePref(context, Util.PREF_RESULT_OF_4_LEFT,0);
        Util.upDatePref(context, Util.PREF_RESULT_OF_4_RIGHT,0);
    }

    /**
     * @param chart
     * @param chartPos
     * @return chartPosition
     */
    public String getChartPosString(int chart, int chartPos) {
        if(chart==-1 || chartPos==-1){
            return "";
        }
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


}