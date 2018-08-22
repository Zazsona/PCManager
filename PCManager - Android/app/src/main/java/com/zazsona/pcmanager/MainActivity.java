package com.zazsona.pcmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity
{

    private static boolean paused;
    private static String ipAndPort;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        paused = false;

        ButtonManager buttonManager = new ButtonManager();
        buttonManager.setMainActivity(this);
        Button btnShutdown = findViewById(R.id.btnShutdown);
        btnShutdown.setOnClickListener(buttonManager);
        Button btnStandby = findViewById(R.id.btnStandby);
        btnStandby.setOnClickListener(buttonManager);
        Button btnLock = findViewById(R.id.btnLock);
        btnLock.setOnClickListener(buttonManager);
        Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(buttonManager);
        Button btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(buttonManager);

        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.setMainActivity(this);
        connectionManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public static String getIP()
    {
        return ipAndPort.substring(0, ipAndPort.indexOf(":"));
    }
    public static String getPort()
    {
        return ipAndPort.substring(ipAndPort.indexOf(":")+1);
    }
    public static boolean isPaused()
    {
        return paused;
    }
    public void setConnectedView(boolean isConnected)
    {
        ImageView ivStatus = findViewById(R.id.statusIcon);

        if (isConnected)
        {
            ivStatus.setImageResource(R.drawable.online);
        }
        else
        {
            ivStatus.setImageResource(R.drawable.offline);
        }
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        System.out.println("App paused.");
        paused = true;

    }
    @Override
    protected void onResume()
    {
        super.onResume();
        System.out.println("App resumed.");
        paused = false;
    }
    public static String encrypt(String source)
    {
        return source; //TODO: Reimplement this
    }
    public static String decrypt(String source)
    {
        return source; //TODO: Reimplement this to be more secure
    }



}
