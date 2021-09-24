package com.example.amharicocr.ui.analysis;


import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnalysisViewModel extends ViewModel {

//    private MutableLiveData<String> mText;
    private MutableLiveData<Bitmap> image;

    public AnalysisViewModel() {
        image = new MutableLiveData<Bitmap>();
    }
    public void setImage(Bitmap image){
        this.image.setValue(image);
    }
    public LiveData<Bitmap> getImage() {
        return image;
    }
}