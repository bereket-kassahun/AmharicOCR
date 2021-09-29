package com.example.amharicocr.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.amharicocr.ui.documents.DocumentItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreference {
    static final String PREFERENCES_KEY = "recents";
    Context context;
    SharedPreferences prefs;
    android.content.SharedPreferences.Editor editor;

    public SharedPreference(Context context){
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public <T> void setList(List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        set(PREFERENCES_KEY, json);
    }

    public void set(String key, String value) {
        editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public List<DocumentItem> getList() {
        List<DocumentItem> arrayItems = null;
        String serializedObject = prefs.getString(PREFERENCES_KEY, null);
        if(serializedObject == null){
            Log.e("---------->>>>>", "nul nul nul nul");
        }else{
            Log.e("---------->>>>>", serializedObject);
        }
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<DocumentItem>>(){}.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
//        Log.e("---------->>>>>", arrayItems.size()+"");
        return arrayItems;
    }

    public void setLanguage(String locale){
        editor = prefs.edit();
        editor.putString("language", locale);
        editor.commit();
    }
    public String getLanguage(){
        return prefs.getString("language", "am");
    }

    public void setFontSize(int size){
        editor = prefs.edit();
        editor.putInt("font_size", size);
        editor.commit();
    }
    public int getFontSize(){
        return prefs.getInt("font_size", 10);
    }
//    static void add(Context context, String path){
//        android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        android.content.SharedPreferences.Editor editor = prefs.edit();
//
//        Gson gson = new Gson();
//        List<String> textList = new ArrayList<String>(get(context));
//        textList.remove(path); // prevent duplicates
//        textList.add(path);
//        String jsonText = gson.toJson(textList);
//        editor.putString(RECENTS_KEY, jsonText);
//        editor.apply();
//    }
//    static ArrayList<String> get(Context context){
//        ArrayList<String> ret = new ArrayList<>();
//        Gson gson = new Gson();
//        android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String jsonText = prefs.getString(RECENTS_KEY, null);
//        String[] text = gson.fromJson(jsonText, String[].class);
//
//        if(text == null) text = new String[]{};
//
//        Log.e("XXXX","recents now contains: ");
//        for(String s: text) {
//            ret.add(s);
//            Log.e("XXXX",s);
//        }
//        return ret;
//    }
}
