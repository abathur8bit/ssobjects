package ssobjects;

/**
 * Created by lbpatterson on 11/20/2014.
 */
public class Stopwatch {
    public long startTime = 0;
    public long stopTime = 0;
    public boolean running = false;

    public Stopwatch() {
        start();
    }

    public void reset() {
        startTime = 0;
        stopTime = 0;
        running = false;
    }
    public void start() {
        running = true;
        startTime = System.currentTimeMillis();
        stopTime = 0;
    }

    public void stop() {
        running = false;
        stopTime = System.currentTimeMillis();
    }

    public long getMilliSeconds() {
        if(running)
            return System.currentTimeMillis()-startTime;

        return stopTime - startTime;
    }

    public String toString() {
        long now = getMilliSeconds();
        long minutes = now/1000/60;
        long seconds = now/1000-minutes*60;
        long milliseconds = now-(minutes*60+seconds)*1000;
        return minutes+"m:"+seconds+"s."+milliseconds+"ms";
    }
}
