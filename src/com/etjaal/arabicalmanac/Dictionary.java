package com.etjaal.arabicalmanac;

public class Dictionary {

    private int id;
    private String reference;
    private String name;
    private String language;
    private int size;
    private boolean installed;

    public Dictionary() {
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

}
