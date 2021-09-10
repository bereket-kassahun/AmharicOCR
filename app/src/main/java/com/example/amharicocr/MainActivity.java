package com.example.amharicocr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Button capture;
    private Button convert;
    private ImageView preview;
    private TextView textView;
    private Bitmap bitmap = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.e("OpenCV", "OpenCV loaded successfully");
//                    imageMat=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    private OCR ocr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        SimpleSample.main(new String[2]);
        capture = findViewById(R.id.capture);
        convert = findViewById(R.id.convert);
        preview = findViewById(R.id.preview);
        textView = findViewById(R.id.textView);

        ocr = new OCR(getApplicationContext(), "amh");

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               dispatchTakePictureIntent();
            }
        });

        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bitmap != null){
                    Mat tmp = convertBitmapToGray(bitmap);
                    Mat temp = otsuTreshold(tmp);
                    List<MatOfPoint> matOfPoints = getContours(temp);
                    drawRectangles(temp, matOfPoints);
                    Bitmap tmp1 = createBitmapfromMat(temp);
                    preview.setImageBitmap(tmp1);
//                    Toast.makeText(getBaseContext(), ocr.getOCRResult(tmp1), Toast.LENGTH_LONG).show();
                    textView.setText(ocr.getOCRResult(tmp1));
                }
            }
        });
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, "unable to open camera app", Toast.LENGTH_LONG).show();
            Log.e("error :", e.getMessage());
        }
    }
    private Mat convertBitmapToGray(Bitmap bitmap){
        Mat ret = new Mat (bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, ret);
        Imgproc.cvtColor(ret, ret, Imgproc.COLOR_RGB2GRAY);
        return ret;
    }

    private Mat otsuTreshold(Mat grayScaleImage){
        Mat ret = new Mat (bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Imgproc.threshold(grayScaleImage, ret,0,255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY_INV);
        return ret;
    }

    private List<MatOfPoint> getContours(Mat thresholdImage){
        Mat dilation = new Mat (bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        final Mat hierarchy = new Mat();
        List<MatOfPoint> matOfPoints = new ArrayList<>();
        Mat rect_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(18, 18));
        Imgproc.dilate(thresholdImage, dilation,rect_kernel);
        Imgproc.findContours(dilation, matOfPoints, hierarchy,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        return matOfPoints;
    }

    private void drawRectangles(Mat destination, List<MatOfPoint> contours){
        Rect rect;
        Scalar scalar = new Scalar(255,255,255);
        for(MatOfPoint cnt : contours){
            rect = Imgproc.boundingRect(cnt);
            Imgproc.rectangle(destination, rect,scalar);
        }
    }
    public Bitmap createBitmapfromMat(Mat snap){
        Bitmap bp = Bitmap.createBitmap(snap.cols(), snap.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(snap, bp);
        return bp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            bitmap = imageBitmap;
            preview.setImageBitmap(imageBitmap);
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.e("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


}