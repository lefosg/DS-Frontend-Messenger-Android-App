package com.ds;

import android.app.Application;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MyApp extends Application {
    private Client client; //make getter and setter

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}