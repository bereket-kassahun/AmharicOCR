package com.example.amharicocr.ui.analysis;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.amharicocr.MainActivityViewModel;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static android.app.Activity.RESULT_OK;
import static java.security.AccessController.getContext;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.google.gson.Gson;

class RecentsList{
    static final String RECENTS_KEY = "recents";
    static void add(Context context, String path){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        List<String> textList = new ArrayList<String>(get(context));
        textList.remove(path); // prevent duplicates
        textList.add(path);
        String jsonText = gson.toJson(textList);
        editor.putString(RECENTS_KEY, jsonText);
        editor.apply();
    }
    static ArrayList<String> get(Context context){
        ArrayList<String> ret = new ArrayList<>();
        Gson gson = new Gson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonText = prefs.getString(RECENTS_KEY, null);
        String[] text = gson.fromJson(jsonText, String[].class);

        if(text == null) text = new String[]{};

        Log.e("XXXX","recents now contains: ");
        for(String s: text) {
            ret.add(s);
            Log.e("XXXX",s);
        }
        return ret;
    }
}

public class AnalysisFragment extends Fragment {

    private OCR ocr;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GET_FILE = 2;
    private MainActivityViewModel mainActivityViewModel;

    private Button capture;
    private Button pick;
    private Button convert;
    private Button btnSave;
    private ImageView preview;
    private TextView textView;
    private Bitmap bitmap = null;

    int finalHeight = 300, finalWidth = 300;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivityViewModel  = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        View root = inflater.inflate(R.layout.analysis_fragment, container, false);

        capture = root.findViewById(R.id.capture);
        pick  = root.findViewById(R.id.from_internal_storage);
        convert = root.findViewById(R.id.convert);
        preview = root.findViewById(R.id.preview);
        textView = root.findViewById(R.id.textView);
        btnSave = root.findViewById(R.id.btn_save);

//        ocr = new OCR(getContext(), "amh");

        mainActivityViewModel.getImage().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                preview.setImageBitmap(bitmap);
                Toast.makeText(getContext(), "lifecycle changed", Toast.LENGTH_SHORT).show();
            }
        });
        mainActivityViewModel.getOcr().observe(getViewLifecycleOwner(), new Observer<OCR>() {
            @Override
            public void onChanged(OCR ocr_) {
                ocr = ocr_;
            }
        });

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
                    mainActivityViewModel.setImage(tmp1);
//                    Toast.makeText(getBaseContext(), ocr.getOCRResult(tmp1), Toast.LENGTH_LONG).show();
                    String t = ocr.getOCRResult(tmp1);
                    textView.setText(t);
                    mainActivityViewModel.setResult(t);

                    if(!t.isEmpty()){
                        btnSave.setEnabled(true);
                    }
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

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textView.getText().toString();
                Log.e("XXXX","saving: "+ text);

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Save as");
                builder.setMessage("What format do you want to save this text?");

                // add the buttons
                builder.setNegativeButton("txt", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ChooserDialog(getContext())
                                // to handle the result(s)
                                .withFilter(true, true)
                                .withChosenListener(new ChooserDialog.Result() {
                                    @Override
                                    public void onChoosePath(String path, File pathFile) {
                                        saveAsTxt(text,path);
                                    }
                                })
                                .build()
                                .show();
                    }
                });
                builder.setNeutralButton("doc", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ChooserDialog(getContext())
                                // to handle the result(s)
                                .withFilter(true, true)
                                .withChosenListener(new ChooserDialog.Result() {
                                    @Override
                                    public void onChoosePath(String path, File pathFile) {
                                        saveAsDoc(text,path);
                                    }
                                })
                                .build()
                                .show();
                    }
                });
                builder.setPositiveButton("pdf", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ChooserDialog(getContext())
                                // to handle the result(s)
                                .withFilter(true, true)
                                .withChosenListener(new ChooserDialog.Result() {
                                    @Override
                                    public void onChoosePath(String path, File pathFile) {
                                        saveAsPdf(text,path);
                                    }
                                })
                                .build()
                                .show();
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        ViewTreeObserver vto = preview.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                preview.getViewTreeObserver().removeOnPreDrawListener(this);
//                finalHeight = preview.getMeasuredHeight();
//                finalWidth = preview.getMeasuredWidth();
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
                mainActivityViewModel.setImage(imageBitmap);

        } else if(requestCode == REQUEST_GET_FILE && resultCode == RESULT_OK){
            Uri uri = null;

            if (data != null) {
                uri = data.getData();
                Toast.makeText(getContext(), "uri: "+ uri.toString(), Toast.LENGTH_LONG).show();

                ContentResolver cR = getContext().getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String type = mime.getExtensionFromMimeType(cR.getType(uri)).toLowerCase();

                if(type.equals("png") || type.equals("jpg") || type.equals("jpeg")){
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "error making bitmap from file", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                else if(type.equals("pdf")){
                    // convert to bitmap
                    try {
                        Log.e("XXXX","getPathFromUri(getContext(),uri)): " + getPathFromUri(getContext(),uri));
                        bitmap = getMergedImagesOfPdf(new File(getPathFromUri(getContext(),uri)));
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "error converting uri to path", Toast.LENGTH_LONG).show();

                        e.printStackTrace();
                    }

                    String realPath = getPathFromUri(getContext(),uri);
                    RecentsList.add(getContext(),realPath);

                } else {
                    Toast.makeText(getContext(), "unsupported file type: "+ type, Toast.LENGTH_LONG).show();
                }

                if(bitmap == null) {
                    Toast.makeText(getContext(), "bitmap is null", Toast.LENGTH_LONG).show();
                    return;
                }

                finalHeight=bitmap.getHeight();
                Bitmap resizedBitmap = getResizedBitmap(bitmap, finalHeight, finalWidth);

                preview.setImageBitmap(resizedBitmap);
                mainActivityViewModel.setImage(resizedBitmap);

            } else {
                Toast.makeText(getContext(), "intent data is null", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void browseDocuments(){
        Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickIntent.setType("*/*");

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

    private ArrayList<Bitmap> pdfToBitmaps(File pdfFile) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();

        try {
            Log.e("XXXX","pdfFile: "  + pdfFile.getAbsolutePath());

            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            Log.e("XXXX","pdfrender after");

            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();
            Log.e("XXXX","pageCount: " + pageCount);
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                bitmaps.add(bitmap);

                // close the page
                page.close();

            }

            // close the renderer
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmaps;

    }

    private Bitmap combineImageIntoOne(ArrayList<Bitmap> bitmap) {
        int w = 0, h = 0;
        for (int i = 0; i < bitmap.size(); i++) {
            if (i < bitmap.size() - 1) {
                w = bitmap.get(i).getWidth() > bitmap.get(i + 1).getWidth() ? bitmap.get(i).getWidth() : bitmap.get(i + 1).getWidth();
            }
            h += bitmap.get(i).getHeight();
        }

        Bitmap temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        int top = 0;
        for (int i = 0; i < bitmap.size(); i++) {
            Log.d("HTML", "Combine: "+i+"/"+bitmap.size()+1);

            top = (i == 0 ? 0 : top+bitmap.get(i).getHeight());
            canvas.drawBitmap(bitmap.get(i), 0f, top, null);
        }
        return temp;
    }

    private Bitmap getMergedImagesOfPdf(File pdfFile){
        ArrayList<Bitmap> bmps = pdfToBitmaps(pdfFile);
        Log.e("XXXX","bmps.size(): " + bmps.size());
        return combineImageIntoOne(bmps);
    }

    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }



    private void saveAsPdf(String str, String path){
        PdfDocument document = new PdfDocument();
        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 1000, 1).create();
        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        //  paint.setColor(Color.RED);
        // canvas.drawCircle(50, 50, 30, paint);
        Date currentTime = Calendar.getInstance().getTime();

        paint.setColor(Color.BLACK);
        // canvas.drawText(wise, 60, 50, paint);
        int y=50;

        canvas.drawText(str, 80, 50, paint);

        canvas = page.getCanvas();
        paint = new Paint();
        // paint.setColor(Color.BLUE);
        // canvas.drawCircle(100, 100, 100, paint);
        document.finishPage(page);
        // write the document content
        String directory_path = path + "/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetPdf = directory_path+"amharic-ocr-"+currentTime+".pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(getContext(), "Pdf file generated in internal storage", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("XXXX", "error "+e.toString());
            Toast.makeText(getContext(), "Something wrong: " + e.toString(),  Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();
    }

    private void saveAsTxt(String str, String path){
        Date currentTime = Calendar.getInstance().getTime();

        String directory_path = path + "/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetTxt = directory_path+"amharic-ocr-"+currentTime+".txt";
        File filePath = new File(targetTxt);

        try {
            FileOutputStream stream = new FileOutputStream(filePath);
            stream.write(str.getBytes());
            stream.close();
            Toast.makeText(getContext(), "Txt file generated in internal storage", Toast.LENGTH_LONG).show();
        } catch(Exception ex){
            Log.e("XXXX", "error "+ex.toString());
            Toast.makeText(getContext(), "Something wrong: " + ex.toString(),  Toast.LENGTH_LONG).show();
        }
    }

    private void saveAsDoc(String str, String path)  {
        Date currentTime = Calendar.getInstance().getTime();

        String directory_path = path + "/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetDoc = directory_path+"amharic-ocr-"+currentTime+".doc";
        File filePath = new File(targetDoc);

        try {
            XWPFDocument xwpfDocument = new XWPFDocument();
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph();
            XWPFRun xwpfRun = xwpfParagraph.createRun();

            xwpfRun.setText(str);

            xwpfDocument.write(fileOutputStream);
            fileOutputStream.close();

            Toast.makeText(getContext(), "Doc file generated in internal storage", Toast.LENGTH_LONG).show();
        } catch(Exception ex){
            Log.e("XXXX", "error "+ex.toString());
            Toast.makeText(getContext(), "Something wrong: " + ex.toString(),  Toast.LENGTH_LONG).show();
        }
    }
   }