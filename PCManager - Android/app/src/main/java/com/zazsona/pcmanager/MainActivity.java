package com.zazsona.pcmanager;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
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

public class MainActivity extends AppCompatActivity {
    private static Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean isPaused;
    private String clientID = "";
    private String newCodeStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File codeFile = new File(MainActivity.this.getFilesDir(), "IP.txt");
        isPaused = false;

        Button btnShutdown = findViewById(R.id.btnShutdown);
        btnShutdown.setOnClickListener(v ->
                                       {
                                           SendCommand shutdownCommand = new SendCommand();
                                           shutdownCommand.execute("0", clientID);
                                       });
        Button btnStandby = findViewById(R.id.btnStandby);
        btnStandby.setOnClickListener(v ->
                                      {
                                          SendCommand standbyCommand = new SendCommand();
                                          standbyCommand.execute("1", clientID);
                                      });
        Button btnLock = findViewById(R.id.btnLock);
        btnLock.setOnClickListener(v ->
                                   {
                                       SendCommand lockCommand = new SendCommand();
                                       lockCommand.execute("2", clientID);
                                   });
        Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(v ->
                                      {
                                          SendCommand restartCommand = new SendCommand();
                                          restartCommand.execute("3", clientID);
                                      });
        Button btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(v ->
                                    {
                                        AlertDialog.Builder aboutBox = new AlertDialog.Builder(MainActivity.this);
                                        aboutBox.setTitle("About");
                                        aboutBox.setMessage("To get started, press \"New Code\" and enter the code shown on your PC.");
                                        aboutBox.setNeutralButton("Ok!", (dialog, which) -> dialog.dismiss());
                                        aboutBox.show();
                                    });

        Button btnNCode = findViewById(R.id.btnNCode);
        btnNCode.setOnClickListener(v -> newCode(false));


        if (!codeFile.exists())
        {
            newCode(true);
        }
        else
        {
            try
            {
                Scanner scanner = new Scanner(codeFile);
                clientID = scanner.next();
                scanner.close();
                new EstablishConnection().execute();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }

    }
    @Override
    protected void onPause()
    {
        super.onPause();
        System.out.println("App paused.");
        isPaused = true;

    }
    @Override
    protected void onResume()
    {
        super.onResume();
        System.out.println("App resumed.");
        isPaused = false;
        if (connection == null && !clientID.equals(""))
        {
            new EstablishConnection().execute();
        }
    }
    private void newCode(boolean onBootArg)
    {
        final boolean onBoot = onBootArg;
        AlertDialog.Builder codeDialog = new AlertDialog.Builder(MainActivity.this);
        codeDialog.setTitle("New Code");
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        codeDialog.setView(input);
        codeDialog.setPositiveButton("Enter", (dialog, which) ->
        {
            newCodeStr = input.getText().toString();
            clientID = newCodeStr;
            File codeFile = new File(MainActivity.this.getFilesDir(), "IP.txt");
            if (!codeFile.exists())
            {
                try
                {
                    codeFile.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            try
            {
                FileWriter fileWriter = new FileWriter(codeFile);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.print(newCodeStr);
                printWriter.close();
                fileWriter.close();
                AlertDialog.Builder aboutBox = new AlertDialog.Builder(MainActivity.this);
                aboutBox.setTitle("Success");
                aboutBox.setMessage("New code set successfully.");
                aboutBox.setNeutralButton("Ok!", (dialog1, which1) -> dialog1.dismiss());
                aboutBox.show();
                if (onBoot)
                {
                    new EstablishConnection().execute();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        });
        codeDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        codeDialog.show();
    }
    private class SendCommand extends AsyncTask<String, Void, Void>
    {
        boolean isSuccess;
        private void runCommand(String code, String id)
        {
            try {
                output.writeObject(encrypt("{\"id\": \""+getIPFromCode(id)+"\", \"cid\": \"" + code + "\"}"));
                output.flush();
                String successCheck = (String) input.readObject();
                if (successCheck.equals("-1")) {
                    isSuccess = true;
                }
            } catch (IOException | ClassNotFoundException e) {
                isSuccess = false;
            }
        }
        @Override
        protected Void doInBackground(String... args) {
            isSuccess = true;
            runCommand(args[0], args[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            AlertDialog.Builder commandNotice = new AlertDialog.Builder(MainActivity.this);
            commandNotice.setTitle("Command Sent");
            commandNotice.setNeutralButton("Got it!", (dialog, which) -> dialog.dismiss());

            if (isSuccess)
            {
                commandNotice.setMessage("Command executed successfully.");
            }
            else
            {
                commandNotice.setMessage("Command sending failed.");
                reportDisconnect();
            }
            commandNotice.show();
        }
    }

    public static String encrypt(String source)
    {
        return source; //TODO: Implement this
    }
    public static String getIPFromCode(String source)
    {
        return source; //TODO: Implement this
    }

    private class EstablishConnection extends AsyncTask<Void, Void, Void> {
        boolean isSuccess = false;

        @Override
        protected Void doInBackground(Void... args) {
            isSuccess = connect();
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (isSuccess)
            {
                System.out.println("Connected!");
                ImageView ivStatus = findViewById(R.id.statusIcon);
                ivStatus.setImageResource(R.drawable.online);
                new ConnectionChecker().execute();
            }
            else
            {
                connection = null;
                if (isPaused)
                {
                    return;
                }
                if (!clientID.equals(""))
                {
                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                new EstablishConnection().execute();
            }

        }
    }

    private boolean connect()
    {
        try
        {
            connection = new Socket();
            File codeFile = new File(MainActivity.this.getFilesDir(), "IP.txt");
            Scanner scanner = new Scanner(codeFile);
            String IP = getIPFromCode(scanner.next());
            scanner.close();
            System.out.println("Connecting to "+IP);
            connection.connect(new InetSocketAddress(IP, 2865), 3000);
            System.out.println("Connected to "+IP);
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            String connectionCode = (String) input.readObject();
            if (connectionCode.equals("1337"))
            {
                return true;
            }
            return true;
        }
        catch (IOException | ClassNotFoundException e)
        {
            try
            {
                if (connection != null)
                {
                    if (!connection.isClosed())
                    {
                        connection.close();
                        connection = null;
                    }
                }
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            return false;
        }
    }

    private class ConnectionChecker extends AsyncTask<Void, Void, Integer>
    {
        @Override
        public Integer doInBackground(Void... args)
        {
            while (isPaused)
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try
            {
                Thread.sleep(1000);
                System.out.println("Ping");
                output.writeObject(encrypt("{\"id\": \""+getIPFromCode(clientID)+"\", \"cid\": \"#\"}"));
                output.flush();
                String successCheck = (String) input.readObject();
                if (successCheck.equals("-1"))
                {
                    return 1;
                }
            }
            catch (InterruptedException | ClassNotFoundException e)
            {
                e.printStackTrace();
                return 0;
            }
            catch (IOException e)
            {
                return 0;
            }
            return 0;
        }
        @Override
        public void onPostExecute(Integer result)
        {
            if (result.equals(0))
            {
                reportDisconnect();
            }
            else
            {
                new ConnectionChecker().execute();
            }
        }
    }
    private void reportDisconnect()
    {
        try
        {
            System.out.println("Connection lost.");
            connection.close();
            ImageView ivStatus = findViewById(R.id.statusIcon);
            ivStatus.setImageResource(R.drawable.offline);
            new EstablishConnection().execute();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }



}
