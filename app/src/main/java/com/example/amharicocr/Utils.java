package com.example.amharicocr;

import android.util.Log;
import android.widget.Toast;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static void saveAsHtml(String str, String path)  {
//        Date currentTime = Calendar.getInstance().getTime();
//
//        String directory_path = path + "/";
//        File file = new File(directory_path);
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//        String targetDoc = directory_path+"amharic-ocr-"+currentTime+".html";
//        File filePath = new File(targetDoc);
//
////        try {
////            XWPFDocument xwpfDocument = new XWPFDocument();
////            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
////            XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph();
////            XWPFRun xwpfRun = xwpfParagraph.createRun();
////
////            xwpfRun.setText(str);
////
////            xwpfDocument.write(fileOutputStream);
////            fileOutputStream.close();
////
////            Toast.makeText(getContext(), "Doc file generated in internal storage", Toast.LENGTH_LONG).show();
////        } catch(Exception ex){
////            Log.e("XXXX", "error "+ex.toString());
////            Toast.makeText(getContext(), "Something wrong: " + ex.toString(),  Toast.LENGTH_LONG).show();
////        }
//
//        try {
//            FileOutputStream out = new FileOutputStream(filePath);
//            byte[] data = str.getBytes();
//            out.write(data);
////            out.flush();
//            out.flush();
//            out.close();
//            Log.e("HTML", "File Saved : " + file.getPath());
////            Toast.makeText(getContext(), "Html file generated in internal storage", Toast.LENGTH_LONG).show();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


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
//            Toast.makeText(getContext(), "html file generated in internal storage", Toast.LENGTH_LONG).show();
        } catch(Exception ex){
            Log.e("XXXX", "error "+ex.toString());
//            Toast.makeText(getContext(), "Something wrong: " + ex.toString(),  Toast.LENGTH_LONG).show();
        }

    }
}
