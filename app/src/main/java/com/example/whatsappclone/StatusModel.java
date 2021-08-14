package com.example.whatsappclone;

class StatusModel {


    String image;
    String name;
    String time;
    String date;
    String uid;

    public  StatusModel(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public StatusModel(String image, String name, String time,String date,String uid) {
        this.image = image;
        this.name = name;
        this.time = time;
        this.date = date;
        this.uid=uid;
    }

}
