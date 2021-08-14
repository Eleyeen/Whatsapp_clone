package com.example.whatsappclone;

class DetailStatusModel {

    public DetailStatusModel(){

    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    String image;

    public DetailStatusModel(String image) {
        this.image = image;
    }
}
