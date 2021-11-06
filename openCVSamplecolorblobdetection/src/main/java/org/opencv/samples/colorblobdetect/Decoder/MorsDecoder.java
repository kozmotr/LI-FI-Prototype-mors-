package org.opencv.samples.colorblobdetect.Decoder;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import org.opencv.samples.colorblobdetect.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ozan on 04.01.2018.
 */




public class MorsDecoder implements Decoder {

    private final static String TAG = "MorsDecoder";

    public static class Key implements Comparable<Key> {

        @Override
        public int compareTo(@NonNull Key o) {
            return 0;
        }
    }


    private static String A = "SL";
    private static String B = "LSSS";
    private static String C = "LSLS";
    private static String D = "LSS";
    private static String E = "S";
    private static String F = "SSLS";
    private static String G = "LLS";
    private static String H = "SSSS";
    private static String I = "SS";
    private static String J = "SLLL";
    private static String K = "LSL";
    private static  String L = "SLSS";
    private static String M = "LL";
    private static String N = "LS";
    private static String O = "LLL";
    private static String P = "SLLS";
    private static String Q = "LLSL";
    private static String R = "SLS";
    private static String S = "SSS";
    private static String T = "L";
    private static String U = "SSL";
    private static String V = "SSSL";
    private static String W = "SLL";
    private static String X = "LSSL";
    private static String Y = "LSLL";
    private static String Z = "LLSS";



    private static final HashMap<String,Character> AllChars = new HashMap<String,Character>(){{
        put(A,'A');
        put(B,'B');
        put(C,'C');
        put(D,'D');
        put(E,'E');
        put(F,'F');
        put(G,'G');
        put(H,'H');
        put(I,'I');
        put(J,'J');
        put(K,'K');
        put(L,'L');
        put(M,'M');
        put(N,'N');
        put(O,'O');
        put(P,'P');
        put(Q,'Q');
        put(R,'R');
        put(S,'S');
        put(T,'T');
        put(U,'U');
        put(V,'V');
        put(W,'W');
        put(X,'X');
        put(Y,'Y');
        put(Z,'Z');

    }};





    @Override
    public  String decode(ArrayList<boolean[]> frameList) {


        String result ="";

        String currentSymbol;
        StringBuilder sb = new StringBuilder();
        char currentCharOfSymbols;

        for (int i = 0 ; i < frameList.size() ; i++)
        {
               currentSymbol = decodeFrame(frameList.get(i));
               Log.d(TAG,"Current Symbol is "+ currentSymbol);

               if (currentSymbol == "START" )
               {
                   continue;
               }
               else if (currentSymbol == "STOP")
               {
                   try {

                       currentCharOfSymbols = decodeSymbols(sb.toString());
                       result += Character.toString(currentCharOfSymbols);
                       Log.d(TAG,"STOP Recevied Last Result : "+result + " appended last Charac is : " + Character.toString(currentCharOfSymbols));
                       sb.setLength(0);
                       continue;
                   }
                   catch (Exception e)
                   {
                       e.printStackTrace();
                       Log.d(TAG,e.toString());
                       Log.d(TAG,"FRAME LIST:");
                       for(int index = 0 ; index < frameList.size(); index++)
                       {
                           String symbol = decodeFrame(frameList.get(index));
                           Log.d("FRAME:" + index + " ", (Boolean.toString(frameList.get(index)[0]) + " " + Boolean.toString(frameList.get(index)[1]) + " " + Boolean.toString(frameList.get(index)[2] )) + "Symbol: " + symbol) ;
                       }
                       Log.d(TAG,"Symbols are : "+ sb.toString());
                       result = "Please locate your device properly and try again!";
                       return result;

                   }
               }
               else
               {
                   sb.append(currentSymbol);
               }
        }
        return result;
    }


    private String decodeFrame(boolean[] frame)  {
        if      (frame[0] && !frame[1] && !frame[2]) return "START"; //*--
        else if (!frame[0] && frame[1] && !frame[2]) return "S"; //-*-
        else if (!frame[0] && !frame[1] && frame[2]) return "L"; // --*
        else if (!frame[0] && frame[1] && frame[2]) return "STOP"; //-**
        else {
            try {
                throw new Exception("Unrecgonized Frame: " + Boolean.toString(frame[0]) + " " + Boolean.toString(frame[1]) + " " + Boolean.toString(frame[2]));
            }
            catch (Exception e)
            {

                e.printStackTrace();
            }
            finally {
                return null;
            }
        }
    }

    private char decodeSymbols(String key) throws Exception
    {
        Character result = AllChars.get(key);
        if (result == null)
        {
            throw new Exception("Given symbol does not match with any character Symbol: " + key );
        }
        return result;


    }
}
