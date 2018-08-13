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
    private static File ipFile;
    private static String ipAndPort;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        paused = false;
        ipFile = new File(MainActivity.this.getFilesDir(), "IP.txt");

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
        Button btnNCode = findViewById(R.id.btnNCode);
        btnNCode.setOnClickListener(buttonManager);

        try
        {
            if (!ipFile.exists())
            {
                ipFile.createNewFile();
                PrintWriter printWriter = new PrintWriter(ipFile);
                printWriter.print("0.0.0.0:00000");
                printWriter.close();
                setNewIP();
            }

            Scanner scanner = new Scanner(ipFile);
            ipAndPort = scanner.next();
            scanner.close();
            ConnectionManager connectionManager = new ConnectionManager();
            connectionManager.setMainActivity(this);
            connectionManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }
        catch (IOException e)
        {
            e.printStackTrace();
            AlertDialog.Builder errorDialog = new AlertDialog.Builder(MainActivity.this);
            errorDialog.setTitle("Error");
            errorDialog.setMessage("An error occurred when trying to access app storage.");
            errorDialog.setNeutralButton("Exit", (dialog1, which1) ->
                                            {
                                                finish();
                                                System.exit(1);
                                            });
            errorDialog.show();
        }
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
    public void setNewIP()
    {
        String oldIP = ipAndPort;
        AlertDialog.Builder ipChangeDialog = new AlertDialog.Builder(MainActivity.this);
        ipChangeDialog.setTitle("New IP & Port (X.Y.Z.W:ABCD)");
        EditText editText = new EditText(MainActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        ipChangeDialog.setView(editText);
        ipChangeDialog.setPositiveButton("Enter", (dialog, which) ->
        {
            try
            {
                String newIP = editText.getText().toString();
                if (validateIP(newIP))
                {
                    PrintWriter printWriter = new PrintWriter(ipFile);
                    printWriter.print(newIP);
                    printWriter.close();
                    ipAndPort = newIP;
                    ConnectionManager.reset();
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        });
        ipChangeDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        ipChangeDialog.show();
        if (oldIP.equals(ipAndPort))
        {
            AlertDialog.Builder invalidIPNotice = new AlertDialog.Builder(MainActivity.this);
            invalidIPNotice.setTitle("Error");
            invalidIPNotice.setMessage("The IP and port you have entered is not valid.");
            invalidIPNotice.setNeutralButton("Oh dear!", (dialog, which) -> dialog.dismiss());
        }
    }
    private static boolean validateIP(String IP)
    {
        String[] ip = IP.substring(0, IP.indexOf(":")).split("\\.");
        String port = IP.substring(IP.indexOf(":"));
        for (String ipAddress : ip)
        {
            if (!((Byte.parseByte(ipAddress) >= 0) && (Byte.parseByte(ipAddress) <= 254)))
            {
                return false;
            }
        }
        if (!((Integer.parseInt(port) >= 0) && (Integer.parseInt(port) <= 65535)))
        {
            return false;
        }
        return true;
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
