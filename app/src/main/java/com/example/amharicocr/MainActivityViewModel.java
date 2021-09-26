package com.example.amharicocr;

import android.graphics.Bitmap;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.amharicocr.sharedpreference.SharedPreference;
import com.example.amharicocr.ui.documents.DocumentItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivityViewModel extends ViewModel {

    //    private MutableLiveData<String> mText;
    private MutableLiveData<Bitmap> image;
    private MutableLiveData<String> result;
    private MutableLiveData<OCR> ocr_;
    private MutableLiveData<List<DocumentItem>> documents;

    public MainActivityViewModel() {
        image = new MutableLiveData<Bitmap>();
        result = new MutableLiveData<>();
        ocr_ = new MutableLiveData<>();
        documents = new MutableLiveData<>();
        documents.setValue(new ArrayList<DocumentItem>());
    }
    public void setImage(Bitmap image){
        this.image.setValue(image);
    }
    public LiveData<Bitmap> getImage() {
        return image;
    }

    public void setResult(String result) {
        this.result.setValue(result);
    }
    public LiveData<String> getResult() {
        return result;
    }

    public void setOcr(OCR ocr) {
        this.ocr_.setValue(ocr);
    }
    public LiveData<OCR> getOcr() {
        return ocr_;
    }

    public void setDocuments(List<DocumentItem> documents){
        this.documents.setValue(documents);
    }
    public LiveData<List<DocumentItem>> getDocuments(){
        return documents;
    }
    public List<DocumentItem> getLists(){
        return documents.getValue();
    }
}