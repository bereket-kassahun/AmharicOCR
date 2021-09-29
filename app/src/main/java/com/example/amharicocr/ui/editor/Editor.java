package com.example.amharicocr.ui.editor;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.amharicocr.MainActivityViewModel;
import com.example.amharicocr.OCR;
import com.example.amharicocr.R;
import com.example.amharicocr.Utils;
import com.example.amharicocr.sharedpreference.SharedPreference;
import com.example.amharicocr.ui.analysis.AnalysisViewModel;
import com.example.amharicocr.ui.documents.DocumentItem;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class Editor extends Fragment {

    boolean from_edit_document = false;
    DocumentItem currentDocumentItem;

    private RichEditor mEditor;
    private TextView mPreview;
    public String htmlText = "";

    private MainActivityViewModel mainActivityViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivityViewModel  = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        View root = inflater.inflate(R.layout.editor_fragment, container, false);

        SharedPreference preference = new SharedPreference(getContext());
//        Toast.makeText(getContext(), preference.getFontSize()+"", Toast.LENGTH_SHORT).show();

        mEditor = (RichEditor) root.findViewById(R.id.editor);
        mEditor.setEditorHeight(700);
        mEditor.setEditorFontSize(preference.getFontSize()+5);
        mEditor.setEditorFontColor(Color.BLACK);
        //mEditor.setEditorBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundResource(R.drawable.bg);
        mEditor.setPadding(10, 10, 10, 10);
        //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        mEditor.setPlaceholder("No text recognized yet...");
        //mEditor.setInputEnabled(false);
//        mPreview = (TextView) root.findViewById(R.id.preview);
        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                Document t = Jsoup.parse(text);
                currentDocumentItem.documentText = t.text();
                htmlText = text;
            }
        });

        root.findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.undo();
            }
        });

        root.findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.redo();
            }
        });

        root.findViewById(R.id.action_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Document doc = Jsoup.parse(htmlText);

                new ChooserDialog(getContext())
                // to handle the result(s)
                .withFilter(true, true)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        saveAsTxt(htmlText, path);
                    }
                })
                .build()
                .show();
            }
        });

        root.findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBold();
            }
        });

        root.findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setItalic();
            }
        });

        root.findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        root.findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        root.findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setStrikeThrough();
            }
        });

        root.findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

        root.findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        root.findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        root.findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        root.findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        root.findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        root.findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        root.findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextColor(isChanged ? Color.BLACK : Color.RED);
                isChanged = !isChanged;
            }
        });

        root.findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
                isChanged = !isChanged;

            }
        });

        root.findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        root.findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        root.findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        root.findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        root.findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        root.findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        root.findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        root.findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        root.findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg",
                        "dachshund", 320);
            }
        });

        root.findViewById(R.id.action_insert_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertYoutubeVideo("https://www.youtube.com/embed/pS5peqApgUA");
            }
        });

        root.findViewById(R.id.action_insert_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertAudio("https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_5MG.mp3");
            }
        });

        root.findViewById(R.id.action_insert_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertVideo("https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4", 360);
            }
        });

        root.findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertLink("https://github.com/wasabeef", "wasabeef");
            }
        });
        root.findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertTodo();
            }
        });
//        Button save = root.findViewById(R.id.save);
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new ChooserDialog(getContext())
//                        // to handle the result(s)
//                        .withFilter(true, true)
//                        .withChosenListener(new ChooserDialog.Result() {
//                            @Override
//                            public void onChoosePath(String path, File pathFile) {
////                                Utils.convertHtmlToDoc(savedText,path);
//                            }
//                        })
//                        .build()
//                        .show();
//
//            }
//        });

//        mainActivityViewModel.getResult().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
////                mEditor.setHtml(s);
//                mEditor.setHtml("nothing");
//                Toast.makeText(getContext(), "changed!", Toast.LENGTH_SHORT).show();
//                savedText = s;
//                from_edit_document = false;
//            }
//        });
        mainActivityViewModel.getCurrentDocument().observe(getViewLifecycleOwner(), new Observer<DocumentItem>() {
            @Override
            public void onChanged(DocumentItem documentItem) {
                mEditor.setHtml(documentItem.documentText);
                currentDocumentItem = documentItem;
//                from_edit_document = true;
            }
        });
        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
//        Toast.makeText(getContext(), "on stop", Toast.LENGTH_SHORT).show();
//        List<DocumentItem> documentItems = mainActivityViewModel.getLists();
//        for(int i = 0; i < documentItems.size(); i++){
//            if(currentDocumentItem.creationDate.equals(documentItems.get(i).creationDate)){
//                documentItems.set(i, currentDocumentItem);
//            }
//        }

    }

    private void saveHtmlAsPdf(String str, String path){

    }
    private void saveAsTxt(String str, String path){
        Date currentTime = Calendar.getInstance().getTime();

        String directory_path = path + "/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetTxt = directory_path+"amharic-ocr-"+currentTime+".html";
        File filePath = new File(targetTxt);

        try {
            FileOutputStream stream = new FileOutputStream(filePath);
            stream.write(str.getBytes());
            stream.close();
            Toast.makeText(getContext(), "HTML file generated in internal storage", Toast.LENGTH_LONG).show();
        } catch(Exception ex){
            Log.e("XXXX", "error "+ex.toString());
            Toast.makeText(getContext(), "Something wrong: " + ex.toString(),  Toast.LENGTH_LONG).show();
        }
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



}
//        final TextView textView = root.root.findViewById(R.id.text_dashboard);
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
