package com.example.whatsappclone;

public class Contacts {

    String name;
    String status;
    String image;
    String uid;
    String state;



    public Contacts(String name, String status, String image, String uid,String state) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.uid = uid;
        this.state=state;
    }


    public Contacts(){


    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

