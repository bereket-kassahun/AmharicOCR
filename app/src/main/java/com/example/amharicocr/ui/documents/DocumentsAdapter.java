package com.example.amharicocr.ui.documents;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;

import com.example.amharicocr.MainActivity;
import com.example.amharicocr.MainActivityViewModel;
import com.example.amharicocr.R;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DocumentsAdapter extends ArrayAdapter<DocumentItem> {
    private PopupMenu popup;
    Context context;
    private MainActivityViewModel mainActivityViewModel;
    public DocumentsAdapter(Context context, ArrayList<DocumentItem> users,MainActivityViewModel mainActivityViewModel) {
        super(context, 0, users);
        this.context = context;
        this.mainActivityViewModel = mainActivityViewModel;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DocumentItem documentItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.document_item, parent, false);
        }
        // Lookup view for data population
        TextView text = (TextView) convertView.findViewById(R.id.document_text);
        TextView date = (TextView) convertView.findViewById(R.id.creation_date);
        String data = documentItem.documentText;
        Button button = convertView.findViewById(R.id.popup_menu);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup = new PopupMenu(getContext(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.documents_menu, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.edit:
                                Toast.makeText(getContext(), "Editing", Toast.LENGTH_SHORT).show();
                                mainActivityViewModel.setCurrentDocument(documentItem);
                                MainActivity.setNavigation(R.id.navigation_editor);
                                break;

                            case R.id.share:
                                /*Create an ACTION_SEND Intent*/
                                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                                /*This will be the actual content you wish you share.*/
                                String shareBody = data;
                                /*The type of the content is text, obviously.*/
                                intent.setType("text/plain");
                                /*Applying information Subject and Body.*/
                                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject));
                                intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                /*Fire!*/
                                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_using)));
                                break;
                            case R.id.delete:
                                mainActivityViewModel.getLists().remove(position);
                                notifyDataSetInvalidated();
                                break;
                            case R.id.search:
                                search(documentItem.documentText);
                                break;
                            case R.id.export:
                                exporter(documentItem.documentText);
                                break;
                        }
                        return false;
                    }
                });
            }
        });
        // Populate the data into the template view using the data object
        int max = Math.min(documentItem.documentText.length(), 30);
        text.setText(documentItem.documentText.substring(0, max));
        date.setText(documentItem.creationDate);
        // Return the completed view to render on screen
        return convertView;
    }
    protected  void search(String text){
//        String url = text;
//        Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
//        i.setData(Uri.parse(url));
//        getContext().startActivity(i);

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH );
        intent.putExtra(SearchManager.QUERY, text);
        getContext().startActivity(intent);
    }
    protected void exporter(String text){
//                String text = textView.getText().toString();
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
//
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
//            Toast.makeText(getContext(), "Pdf file generated in internal storage", Toast.LENGTH_LONG).show();
            androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Pdf file generated in internal storage");
            alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
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
//            Toast.makeText(getContext(), "Txt file generated in internal storage", Toast.LENGTH_LONG).show();
            androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Txt file generated in internal storage");
            alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
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

//            Toast.makeText(getContext(), "Doc file generated in internal storage", Toast.LENGTH_LONG).show();
            androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Doc file generated in internal storage");
            alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        } catch(Exception ex){
            Log.e("XXXX", "error "+ex.toString());
            Toast.makeText(getContext(), "Something wrong: " + ex.toString(),  Toast.LENGTH_LONG).show();
        }
    }
}