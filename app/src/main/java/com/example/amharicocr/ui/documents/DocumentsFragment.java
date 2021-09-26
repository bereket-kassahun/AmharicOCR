package com.example.amharicocr.ui.documents;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.amharicocr.MainActivityViewModel;
import com.example.amharicocr.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class DocumentsFragment extends Fragment {

    ListView listView;
    DocumentsAdapter documentsAdapter;

    ArrayList<DocumentItem> sampleLists = new ArrayList<>();;

    private MainActivityViewModel mainActivityViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivityViewModel  = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        View root = inflater.inflate(R.layout.documents_fragment, container, false);
        listView = root.findViewById(R.id.documents_list);
        mainActivityViewModel.getDocuments().observe(getViewLifecycleOwner(), new Observer<List<DocumentItem>>() {
            @Override
            public void onChanged(List<DocumentItem> documentItems) {
                sampleLists = (ArrayList<DocumentItem>) documentItems;
            }
        });
//        if(sampleLists == null){
////            Toast.makeText(getContext(), "sample list is empty", Toast.LENGTH_SHORT).show();
//            Log.e("========>>>>", "sample list is empty");
//        }else{
//            Log.e("========>>>>", sampleLists.get(0).toString());
//        }
        sampleLists = (ArrayList<DocumentItem>) mainActivityViewModel.getLists();
        sampleLists.add(new DocumentItem("sample...", "today"));
        documentsAdapter = new DocumentsAdapter(getContext(), sampleLists);
// Attach the adapter to a ListView
        listView.setAdapter(documentsAdapter);
        return root;
    }
}