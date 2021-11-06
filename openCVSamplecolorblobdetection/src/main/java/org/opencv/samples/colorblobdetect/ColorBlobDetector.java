package org.opencv.samples.colorblobdetect;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.colorblobdetect.utils.TimeHelper;

public class ColorBlobDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.1;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,50,50,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;
        /*
        // in this block set a hvs spectrum to show to the user color range.
        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp); // create a hvs to show range of selected color.
        }

        //converts an image from one color space to another
        // here we convert hvs to rgb , HVS2RGB_FULL
        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
        */
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

    TimeHelper helperpyrDown1 = new TimeHelper("BlobDetector",30,"pyrDown1");
    TimeHelper helperpyrDown2 = new TimeHelper("BlobDetector",30,"pyrDown2");
    TimeHelper process_cvtColor = new TimeHelper("BlobDetector",30,"cvtColor");
    TimeHelper process_inRange = new TimeHelper("BlobDetector",30,"inRange");



    //Imgproc.resize(rgbaImage,mPyrDownMat,new Size(0,0),0.25,0.25,Imgproc.INTER_NEAREST); // you can use instead of Ganuis pyramid downside opreation , much faster :)
    public void process(Mat rgbaImage) {
        helperpyrDown1.start();

        if (false)
        {

            Imgproc.pyrDown(rgbaImage, mPyrDownMat); // downside the image to it's half of it
            helperpyrDown1.end();
            helperpyrDown2.start();

            //
            Imgproc.pyrDown(mPyrDownMat, mPyrDownMat); //downside the image to it's half of it again.
            helperpyrDown2.end();

        }
        else
        {
            Imgproc.resize(rgbaImage,mPyrDownMat,new Size(0,0),0.25,0.25,Imgproc.INTER_NEAREST); // you can use instead of Ganuis pyramid downside opreation , much faster :)

        }


        // Convert bgr image to hsv image
        process_cvtColor.start();
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        process_cvtColor.end();


        process_inRange.start();
        // Theresold the hsv image and keep only the rex pixels
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);

        process_inRange.end();


        Imgproc.dilate(mMask, mDilatedMask, new Mat());




        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                mContours.add(contour);
            }
        }
        contours.clear();
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }
}
