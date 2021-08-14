package com.example.whatsappclone;

import java.util.List;

public interface IFirebaseLoadDone {


    void onIFirebaseLoadSuccess(List<DetailStatusModel> statusModels);

    void onIFirebaseLoadFailed(String  message);


}
