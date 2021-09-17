package com.example.amharicocr.ui.analysis;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.amharicocr.OCR;
import com.example.amharicocr.R;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class AnalysisFragment extends Fragment {
    private OCR ocr;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GET_FILE = 2;
    private AnalysisViewModel dashboardViewModel;

    private Button capture;
    private Button pick;
    private Button convert;
    private ImageView preview;
    private TextView textView;
    private Bitmap bitmap = null;


    int finalHeight, finalWidth;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(AnalysisViewModel.class);
        View root = inflater.inflate(R.layout.analysis_fragment, container, false);

        capture = root.findViewById(R.id.capture);
        pick  = root.findViewById(R.id.from_internal_storage);
        convert = root.findViewById(R.id.convert);
        preview = root.findViewById(R.id.preview);
        textView = root.findViewById(R.id.textView);

        ocr = new OCR(getContext(), "amh");

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               dispatchTakePictureIntent();
            }
        });

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browseDocuments();
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
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        ViewTreeObserver vto = preview.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                preview.getViewTreeObserver().removeOnPreDrawListener(this);
                finalHeight = preview.getMeasuredHeight();
                finalWidth = preview.getMeasuredWidth();
//                tv.setText("Height: " + finalHeight + " Width: " + finalWidth);

                return true;
            }
        });
        return root;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                bitmap = imageBitmap;
                preview.setImageBitmap(imageBitmap);
        }else if(requestCode == REQUEST_GET_FILE && resultCode == RESULT_OK){
            if(data.getData() != null){
                Uri uri=data.getData();
                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap resizedBitmap = getResizedBitmap(imageBitmap, finalHeight, finalWidth);
                preview.setImageBitmap(resizedBitmap);
            }
            Toast.makeText(getContext(), "hurray", Toast.LENGTH_LONG).show();

        }
    }

    private void browseDocuments(){
        String[] mimeTypes = {"image/jpeg", "image/png"};
        final Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .setType("image/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(pickIntent, REQUEST_GET_FILE);
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        float scale = Math.max(scaleWidth, scaleHeight);
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(1, scaleHeight);
        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }
}