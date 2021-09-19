package com.example.amharicocr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
static {
    System.setProperty("javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
    System.setProperty("javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
    System.setProperty("javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
    System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
    System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
    System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");}
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_APP_PERMISSION = 2;

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

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_analysis).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
//        SimpleSample.main(new String[2]);
//        capture = findViewById(R.id.capture);
//        convert = findViewById(R.id.convert);
//        preview = findViewById(R.id.preview);
//        textView = findViewById(R.id.textView);
//
//        ocr = new OCR(getApplicationContext(), "amh");
//
//        capture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               dispatchTakePictureIntent();
//            }
//        });
//
//        convert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(bitmap != null){
//                    Mat tmp = convertBitmapToGray(bitmap);
//                    Mat temp = otsuTreshold(tmp);
//                    List<MatOfPoint> matOfPoints = getContours(temp);
//                    drawRectangles(temp, matOfPoints);
//                    Bitmap tmp1 = createBitmapfromMat(temp);
//                    preview.setImageBitmap(tmp1);
////                    Toast.makeText(getBaseContext(), ocr.getOCRResult(tmp1), Toast.LENGTH_LONG).show();
//                    textView.setText(ocr.getOCRResult(tmp1));
//                }
//            }
//        });

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                REQUEST_APP_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_APP_PERMISSION: {
                boolean isPermissionMissing = false;
                if(grantResults.length == 0) isPermissionMissing = true;

                for(int r :grantResults){
                    if(r != PackageManager.PERMISSION_GRANTED) isPermissionMissing = true;
                }

                if(isPermissionMissing)
                    Toast.makeText(MainActivity.this, "A permission has not been granted", Toast.LENGTH_SHORT).show();

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
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