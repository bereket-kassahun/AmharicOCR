package com.example.amharicocr.ui.documents;

import android.content.Context;
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

import com.example.amharicocr.R;

import java.util.ArrayList;

public class DocumentsAdapter extends ArrayAdapter<DocumentItem> {
    private PopupMenu popup;
    public DocumentsAdapter(Context context, ArrayList<DocumentItem> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DocumentItem item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.document_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.document_text);
        TextView tvHome = (TextView) convertView.findViewById(R.id.creation_date);
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

                            case R.id.one:
                                Toast.makeText(getContext(), "Editing", Toast.LENGTH_SHORT).show();
                                break;

                            case R.id.two:
                                break;

                            case R.id.three:
                                break;

                        }
                        return false;
                    }
                });
            }
        });
        // Populate the data into the template view using the data object
        tvName.setText(item.documentText);
        tvHome.setText(item.creationDate);
        // Return the completed view to render on screen
        return convertView;
    }
}