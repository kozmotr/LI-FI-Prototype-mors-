package org.opencv.samples.colorblobdetect.utils;


import android.os.SystemClock;
import java.util.ArrayList;
import android.util.Log;

import org.opencv.BuildConfig;
import org.opencv.core.Core;

import java.util.HashMap;


/**
 * Created by ozan on 28.12.2017.
 */



public class TimeHelper {
    private long mStart;
    private long mEnd;
    private long mDiff;
    private double mTime ;
    private String mFunctionName;
    private static double mFrequency = Core.getTickFrequency();

    private int mCountedTimesSize;
    private int mAvarageCount ;
    private long[] mCountedTimes;
    private String mTag;
    private ArrayList<String> mCalculatedArrayList = new ArrayList<String>();
    public TimeHelper(String tag ,int avarageCount , String fName)
    {
        mTag = tag;
        mFunctionName = fName;
        mAvarageCount = avarageCount;
        mCountedTimes = new long[avarageCount];
    }

    final static int avarage_size = 30;
    static int  size = avarage_size;
    static HashMap<String,HashMap<String,Double>> functions = new HashMap<>(); //hashmap that stores hashmap with their function name


    public void start()
    {
        if(ConfigBuild.DEBUG)    mStart = Core.getCPUTickCount();
    }

    public void end( )
    {
        if (ConfigBuild.DEBUG) {

            mEnd = Core.getCPUTickCount();
            mDiff = mEnd - mStart;
            mTime = mDiff / mFrequency; // get time in seconds
            mTime = mTime * 1000000000; // convert it to ns
            dumpTime(mTime, mFunctionName);
        }
    }

    public static void  dumpTime(double time , String functionName)
    {
        HashMap<String,Double> function_times = functions.get(functionName);
        if(function_times == null)
        {
            function_times = new HashMap<>();
            functions.put(functionName,function_times);
        }
        int current_size = function_times.size();
        function_times.put(functionName+current_size , time);
        current_size++; // update current size .

        if (current_size > avarage_size) // dump collected times
        {
            double fTime = 0;
            int avarage_time = 0;
            for(int i = 0 ; i < current_size ; i++)
            {
                avarage_time += function_times.get(functionName+i);
                function_times.remove(functionName+i) ; // remove it from the hashmap.

            }
            avarage_time = avarage_time / current_size;
            Log.d("Function name\t: " + functionName , "Time is\t" + avarage_time);
        }
    }

    private static long currentTime = 0;
    private static int calledCount = 0;
    private static double totalTime = 0;
    private static long calledTime = 0;
    private static double  fps= 0;
    public static void calculateFps()
    {
        calledCount++;
        if(calledTime == 0)
        {
            calledTime = Core.getTickCount();
        }
        else
        {
            currentTime = Core.getTickCount();
            totalTime = currentTime - calledTime;
            totalTime = totalTime / mFrequency;
            if (totalTime >= 1)
            {
                Log.d("total time " + totalTime + "FPS", ":"+calledCount);
                calledTime = 0;
                calledCount = 0;
            }
        }

    }



}
