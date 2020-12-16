package com.givevision.rochesightchart;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.System.exit;

/**
 *
 */
public class LearnMachine {

    private static final int NBR_OF_CHARACTERS=4;
    //Visual Angle(min), LogMAR, Approximate M-units, Gap (mm), Image Outer diameter (mm), pixels, E grey, square grey
    private float[][] OPTO_TYPE={
            {1f,    0f,   4f,   1.16f, 5.82f,    1f,  250f, 250f},
            {1.25f, 0.1f, 5f,   1.46f, 7.32f,    2f,  250f, 250f},
            {1.6f,  0.2f, 6.3f, 1.84f, 9.22f,    3f,  250f, 250f},
            {2f,    0.3f, 8f,   2.34f, 11.61f,   4f,  250f, 250f},
            {2.5f,  0.4f, 10f,  2.92f, 14.61f,   5f,  250f, 250f},
            {3.2f,  0.5f, 12.5f,3.68f, 18.40f,   6f,  250f, 250f},
            {4f,    0.6f, 16f,  4.63f, 23.16f,   7f,  250f, 250f},
            {5f,    0.7f, 20f,  5.83f, 29.16f,   8f,  250f, 250f},
            {6.3f,  0.8f, 25f,  7.34f, 36.71f,   9f,  250f, 250f},
            {8f,    0.9f, 32f,  9.24f,  46.21f,  10f, 250f, 250f},
            {10f,   1f,   40f,  11.64f, 58.18f,  11f, 250f, 250f},
            {12.5f, 1.1f, 50f,  14.69f, 72.24f,  12f, 250f, 250f},
            {16f,   1.2f, 63f,  18.33f, 92.20f,  13f, 250f, 250f},
            {20f,   1.3f, 80f,  23.26f, 116.18f, 14f, 250f, 250f},
            {25f,   1.4f, 100f, 29.08f, 145.40f, 15f, 250f, 250f},
            {30f,   1.5f, 125f, 34.89f, 174.45f, 16f, 250f, 250f},
            {40f,   1.6f, 160f, 46.52f, 232.60f, 17f, 250f, 250f}
    };

    //represent charts series: random "down", "up", "left", "right" string in array of 4 by series
    private ArrayList<String[]> charts = new ArrayList<>();
    //represent results by eye: chart,series result
    private ArrayList<int[]> results_left = new ArrayList<>();
    private ArrayList<int[]> results_right = new ArrayList<>();
    //represent optotypes series: Visual Angle(min), LogMAR, Approximate M-units, Gap (mm),Outer diameter (mm)
    private ArrayList<float[]> optotypes =new ArrayList<>();
    private Context context;

    /**
     *
     */
    public LearnMachine(Context ctx){
        context=ctx;
        for(int i=OPTO_TYPE.length-1; i>=0;i--){
            optotypes.add(OPTO_TYPE[i]);
        }
//        for(int i=104; i>0;i--){
//            float[] optotype= new float [] {50f, 1.7f, 200f, 58.16f, 290.80f, i*1f, 113f, 138f}; //0
//            optotypes.add(optotype);
////            Log.i(Util.LOG_TAG_LEARN, "optotypes created= "+i);
//        }
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "constructor optotypes sizes= "+optotypes.size());        }
        Random r = new Random();
        String[] array=new String [] {"down", "up", "left", "right"};
        for(int j=0; j<optotypes.size();j++){
//            Log.i(Util.LOG_TAG_LEARN, "optotypes charts= "+j);
            String[] chart=new String []{"","","",""};
            for(int k=0; k<NBR_OF_CHARACTERS; k++) {
                int p = r.nextInt(4);
                chart[k] = array[p];
            }
            charts.add(j, chart);
        }
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "constructor optotypes sizes= "+optotypes.size()+ " charts sizes= "+charts.size());        }

//        for(int m=0; m<optotypes.size();m++){
//            Log.i(Util.LOG_TAG_LEARN, "optotypes done= "+m);
//            String[] chart=charts.get(m);
//            for(int n=0; n<NBR_OF_CHARACTERS; n++){
//                if (Util.DEBUG) {
//                    Log.i(Util.LOG_TAG_LEARN, "serie= "+m+" chart= "+n+" value= "+chart[n]);        }
//            }
//        }
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "constructor done"); }
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
        if(eye==0){
            //find last good series
            for (int p=0; p<results_left.size(); p++){
                if(isResultOk(p,eye)){
                    resultOkPos=p;
                }
            }
            if(resultOkPos>-1 && resultOkPos<results_left.size()){
                float[] array=  optotypes.get(resultOkPos);
                if(array[2]-(int)array[2]>0){
                    return String.format("%.1f", array[1]);
                }
                return String.valueOf(array[1]);
            }else{
                return "no reading";
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
                    return String.format("%.1f", array[1]);
                }
                return String.valueOf(array[1]);
            }else{
                return "no reading";
            }
        }else{
            return "no reading";
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
            if(resultOkPos>-1 && resultOkPos<results_left.size()){
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

    /**
     * @param optotype
     * @return number of pixels
     */
    public float getOptotypePixels(int optotype) {
        if(optotypes.size()>optotype) {
            return optotypes.get(optotype)[5];
        }
        return -1;
    }

    /**
     * @param optotype
     * @return grey level of E charatcer
     */
    public float getOptotypeEgrey(int optotype) {
        if(optotypes.size()>optotype) {
            return optotypes.get(optotype)[6];
        }
        return -1;
    }

    /**
     * @param optotype
     * @return grey level of E charatcer
     */
    public float getOptotypeSquaregrey(int optotype) {
        if(optotypes.size()>optotype) {
            return optotypes.get(optotype)[7];
        }
        return -1;
    }

    /**
     * @param  contrastRightResult in M-init
     * @return logM in String
     */
    public String getLogMFromMunit(String contrastRightResult) {
        for(int j=0; j<optotypes.size();j++){
            float[] array=  optotypes.get(j);
            if(array[2]==Float.parseFloat(contrastRightResult)){
                return String.valueOf(array[1]);
            }
        }
        return "no reading";
    }
}