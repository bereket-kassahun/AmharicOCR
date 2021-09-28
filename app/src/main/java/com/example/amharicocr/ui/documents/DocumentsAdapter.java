package com.example.amharicocr.ui.documents;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.ArrayList;

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
}