package com.etjaal.arabicalmanac.Objects;

public class Dictionary {

    private int id;
    private String reference;
    private String name;
    private String language;
    private String downloadLink;
    private boolean installed;
    private int size;

    public Dictionary() {

    }

    public Dictionary(String reference, String name) {
	this.reference = reference;
	this.name = name;
	downloadLink = "https://dl.dropboxusercontent.com/u/63542577/"
		+ reference + ".zip";

    }

    public Dictionary(String ref, String name, String lang, boolean installed,
	    int size) {
	this.reference = ref;
	this.name = name;
	this.language = lang;
	downloadLink = "https://dl.dropboxusercontent.com/u/63542577/" + ref
		+ ".zip";
	this.installed = installed;
	this.size = size;
    }

    public int getId() {
	return id;
    }

    public String getReference() {
	return reference;
    }

    public String getName() {
	return name;
    }

    public String getLanguage() {
	return language;
    }

    public int getSize() {
	return size;
    }

    public boolean isInstalled() {
	return installed;
    }

    public String getDownloadLink() {
	return downloadLink;
    }

    public void setId(int id) {
	this.id = id;
    }

    public void setReference(String reference) {
	this.reference = reference;
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setLanguage(String language) {
	this.language = language;
    }

    public void setSize(int size) {
	this.size = size;
    }

    public void setInstalled(boolean installed) {
	this.installed = installed;
    }

    public void setDownloadLink(String downloadLink) {
	this.downloadLink = downloadLink;
    }

    public String toString() {
	return name + " (" + language + ")";
    }
}
