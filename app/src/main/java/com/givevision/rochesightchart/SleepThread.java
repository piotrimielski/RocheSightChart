package com.givevision.rochesightchart;

/**
 * Created by work on 19/03/2018.
 */

public class SleepThread extends Thread {
    long duration;
    public SleepThread(long duration){
        this.duration=duration;
    }

    public void run(){
//        Looper.prepare();
        try {
            sleep(duration);
        } catch (InterruptedException e) {
            System.out.println("Thread Interrupted! "+e);
        }
//        Looper.loop();
    }
}