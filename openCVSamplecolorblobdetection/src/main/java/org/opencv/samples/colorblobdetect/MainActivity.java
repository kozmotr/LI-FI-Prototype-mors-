package org.opencv.samples.colorblobdetect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.colorblobdetect.Decoder.Decoder;
import org.opencv.samples.colorblobdetect.Decoder.MorsDecoder;
import org.opencv.samples.colorblobdetect.utils.TimeHelper;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "LIFI";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private MyColorBlobDetector  mGreenDetector;
    private MyColorBlobDetector  mRedDetector;
    private MyColorBlobDetector  mWhiteDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private TimeHelper           mCamareFrameTimer;
    private CameraBridgeViewBase mOpenCvCameraView;

    private String mMessage ;
    private Scalar touchedColor = null;
    private int showedFpsCount = 0;

    private  boolean isGreenCaptured = false;
    private  boolean isRedCaptured = false;
    private  boolean isWhiteCaptured = false;
    private  int mGreenCapturedCount = 0;
    private  int mRedCapturedCount = 0;
    private  int mWhiteCapturedCount = 0;

    private Scalar mRedColorHsv  = new Scalar(249.07,196.21,251.21,0.0);
    private Scalar mRedColorHsv2  = new Scalar(246.578125,114.40625,137.05375,0.0);
    private Scalar mGreenColorHsv = new Scalar(67.578125,136.9375,111.8225,0);
    private Scalar mGreenColorHsv2 = new Scalar(67.359375,118.265625,72.578125,0);
    private Scalar mGreenColorHsv3 = new Scalar(68.96875,82.421875,117.546875,0);
    private Scalar mWhiteColorHvs = new Scalar(167.375,90.3125,162.125,0.0);
    private Scalar mWhiteColorHvs2 = new Scalar(146.375,72.3125,227.890625,0.0);

    private Decoder morsDecoder = null;
    private ArrayList<boolean[]> frameToDecode = null;
    private String decodedString = null;

    private final Scalar RED = new Scalar(255,0,0);

    int width,height;
    Rect roi;
    Mat roi_mat;
    boolean is_touched = false;


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                    init();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }




    private void init()
    {

        mGreenDetector = new MyColorBlobDetector(mGreenColorHsv3);
        mRedDetector = new MyColorBlobDetector(mRedColorHsv2);
        mWhiteDetector = new MyColorBlobDetector(mWhiteColorHvs2);

        morsDecoder = new MorsDecoder();
        frameToDecode = new ArrayList<>();
        is_touched = true;
        width =960;
        height =720;
        roi = new Rect(width/4,height/2,width/2,height/6);
        roi_mat = new Mat(height,width,CvType.CV_8UC4,new Scalar(0 ,0,width/2,height/2));


    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        mCamareFrameTimer = new TimeHelper(TAG,30,"onCameraFrame");


    }

    public void onCameraViewStopped() {
        mRgba.release();
    }



    public boolean onTouch(View v, MotionEvent event) {

        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);


        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        Scalar selectedColor = new Scalar(mBlobColorHsv.val[0],mBlobColorHsv.val[1],mBlobColorHsv.val[2]);
        mWhiteDetector = new MyColorBlobDetector(selectedColor);
        Log.d(TAG,"Touched color val is val0: " + mBlobColorHsv.val[0] + " val1: " + mBlobColorHsv.val[1] + " val2: " + mBlobColorHsv.val[2]);
        setMessageToShow("Touched color ");

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
        touchedColor = mBlobColorRgba;
        touchedColor = new Scalar(mBlobColorRgba.val[0],mBlobColorRgba.val[1],mBlobColorRgba.val[2]);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        //mDetector.setHsvColor(mBlobColorHsv);

        //get spectrum here and resize it to our mSpectrum size.
        //Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }



    class RunIt implements Runnable {
        int my_val;
        RunIt (int val)
        {
            this.my_val = val;
        }
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, "light is captured for " + this.my_val + " time", Toast.LENGTH_SHORT).show();

        }

    }

    class MessageCarrier implements Runnable{
        String message;
        MessageCarrier(String val)
        {
            this.message = val;
        }
        @Override
        public void run() {
            Toast.makeText(MainActivity.this,this.message , Toast.LENGTH_SHORT).show();
        }
    }


    private void setMessageToShow(String newMessage)
    {
        mMessage = newMessage;
        showedFpsCount = 0;
    }

    private void showMessage()
    {
        showedFpsCount ++;
        if (showedFpsCount >= 20)
        {
            mMessage = null;
            return;
        }
        int width = mRgba.width();
        Mat messageLabel = mRgba.submat(0, 68, 0,  width);
        Imgproc.putText(messageLabel,mMessage,new Point(30,30),Core.FONT_HERSHEY_COMPLEX_SMALL,2.0,RED);

    }

    boolean is_preamble_start = false;
    boolean is_preamble_finish = false;

    boolean rl=false,gl=false,wl=false; //redled greenled whiteled
    boolean rgw=false,rg=false,rw=false,r=false,gw=false,g=false,w=false,b=true; // redgrenwhite,redgreen,redwhite,red,greenwhite,green,white,black
    boolean[] led_status;
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //TimeHelper.calculateFps();
        //if (mIsColorSelected == false) return inputFrame.rgba();
        //mCamareFrameTimer.start();
        mRgba = inputFrame.rgba();



        List<MatOfPoint> redContours ;
        List<MatOfPoint> greenContours;
        List<MatOfPoint> whiteContours;

        //draw rectangle
        Imgproc.rectangle(mRgba,roi.tl(),roi.br(),RED,2,8,0);
        //get image in the rectangle into to roi_mat
        mRgba.submat(roi).copyTo(roi_mat.submat(roi));;

        //process roi_mat to find colors
        redContours = mRedDetector.process(roi_mat);
        greenContours = mGreenDetector.process(roi_mat);
        whiteContours = mWhiteDetector.process(roi_mat);


        //detect led status
        rl = redContours.size()>=1 ? true:false; // is red led is open
        gl = greenContours.size()>=1 ? true:false; // is green led is open
        wl = whiteContours.size()>=1 ? true:false; // is white led is open


        if ( is_preamble_finish && is_preamble_start && redContours.size() >= 1 && greenContours.size() >= 1 && whiteContours.size() >= 1) // stop is read
        {
            Log.d(TAG,"Finish Preamble Captured");
            b= false;
            is_preamble_start = false;
            is_preamble_finish = false;
            //Decode collected frame here.
            decodedString = morsDecoder.decode(frameToDecode);
            this.runOnUiThread(new MessageCarrier(decodedString));
            frameToDecode.clear();
        }

        if (!b && !rl && !gl && !wl){ // all leds are off which means we're ready to capture next frame..
            rgw = rg = rw = r = gw = g = w = false ;
            b=true;
            Log.d(TAG,"Black is captured");
        }

        /*
        if (is_preamble_read){
            if ( !(!rl && gl && !wl) && !(rl && gl && wl && !rgw) && !(rl && gl && !rg) && !(rl && wl && !rw) && !(rl && !r) && !(gl && wl && !gw) && !(gl && !g) && !(wl && !w)){
                Log.d(TAG,"A duplicated frame is detected");
            }
            else{
                led_status = new boolean[]{rl,gl,wl};
                frameToDecode.add(led_status);
            }
        }
        */
        //the below if-else statements could have been written as above. But for the sake of readability better to use expanded version as below
        if(is_preamble_start && is_preamble_finish){
            led_status = new boolean[]{wl,rl,gl};


            if(rl && gl && wl && b){ // capture rgw status
                b=false;
                rgw=true;
                frameToDecode.add(led_status);
            }
            else if (!wl && rl && gl && b){ // capture rg
                rg=true;
                Log.d(TAG,"rg is captured(Stop)");
                b=false;
                frameToDecode.add(led_status);
            }
            else if (!gl && rl && wl && b){ // capture rw
                Log.d(TAG,"rw is captured(Unknowon)");
                rw=true;
                b=false;
                frameToDecode.add(led_status);
            }
            else if (!rl && gl && wl && b){ // capture gw
                Log.d(TAG,"gw is captured(Unknowon)");
                gw=true;
                b=false;
                frameToDecode.add(led_status);
            }
            else if (!wl && !gl && rl && b){ // capture r
                Log.d(TAG,"r is captured(Short)");
                r=true;
                b=false;
                frameToDecode.add(led_status);
            }

            else if(!rl && !wl && gl && b){ // capture g
                Log.d(TAG,"green is captured(Long)");
                g=true;
                b=false;
                frameToDecode.add(led_status);
            }
            else if (!rl && !gl && wl && b){ // capture w
                Log.d(TAG,"w is captured(Start)");
                w=true;
                b=false;
                frameToDecode.add(led_status);
            }
            else{ // duplicated frame of previous frame . This happens if we capture the same led status with previous frame which means the led source could be faster..
                //Log.d(TAG,"A duplicated frame is captured , skipping.");
            }

        }


        if(b && !is_preamble_start && redContours.size() >= 1 && greenContours.size() >= 1 && whiteContours.size() >= 1) // start is read
        {
            Log.d(TAG,"Start Preaamble is captured");
            setMessageToShow("Preamble is read");
            is_preamble_finish = false;
            is_preamble_start = true;
        }

        if (is_preamble_start && !is_preamble_finish && ((redContours.size() < 1) && (greenContours.size() < 1) && (whiteContours.size() < 1) ) ) // detect black frame for understanding preamble is finished.
        {
            is_preamble_finish = true;
            b = true;
        }




        if (mMessage != null) showMessage();
        //mDetector.process(mRgba);






        Imgproc.drawContours(mRgba, redContours, -1, CONTOUR_COLOR);
        Imgproc.drawContours(mRgba, greenContours, -1, CONTOUR_COLOR);
        Imgproc.drawContours(mRgba, whiteContours, -1, CONTOUR_COLOR);

        /**
         * Here we put a rectange on the left above to show selected color or put a text

        int width = mRgba.width();
        Mat colorLabel = mRgba.submat(0, 68, 0,  width);
        Imgproc.putText(colorLabel,"deneme",new Point(30,30),Core.FONT_HERSHEY_COMPLEX_SMALL,2.0,new Scalar(200,20));
        //colorLabel.setTo(mBlobColorRgba);
         */
        Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
        mSpectrum.copyTo(spectrumLabel);

        mCamareFrameTimer.end();
        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}