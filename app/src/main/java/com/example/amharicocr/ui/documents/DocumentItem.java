package com.example.amharicocr.ui.documents;

public class DocumentItem {
    public String documentText;
    public String creationDate;
    public String path;

    public DocumentItem(String documentText, String creationDate){
        this.documentText = documentText;
        this.creationDate = creationDate;
    }
}
