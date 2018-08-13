package com.zazsona.pcmanager;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectionManager extends AsyncTask<Void, Void, Byte>
{
    private static final byte CONNECTION_ESTABLISHED = -1;
    private static final byte CONNECTION_LOST = -2;
    private static final byte CONNECTION_ONGOING = -3;

    private static Socket connection;
    private static ObjectOutputStream output;
    private static ObjectInputStream input;

    private static MainActivity mainActivity;
    public static void reset()
    {
        try
        {
            connection.close();
            connection = null;
            output.close();
            output = null;
            input.close();
            input = null;
        }
        catch (IOException | NullPointerException e)
        {
            e.printStackTrace();

        }
    }
    public static void setMainActivity(MainActivity mainActivityArg)
    {
        mainActivity = mainActivityArg;
    }
    public static Socket getConnection()
    {
        return connection;
    }
    public static ObjectOutputStream getOutput()
    {
        return output;
    }
    public static ObjectInputStream getInput()
    {
        return input;
    }

    @Override
    protected Byte doInBackground(Void... args)
    {
        if (connection == null)
        {
            while (true)
            {
                boolean isSuccess = connect();
                if (isSuccess)
                {
                    return CONNECTION_ESTABLISHED;
                }
                else //Wait, then retry
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

            }
        }
        else
        {
            try
            {
                while (MainActivity.isPaused())
                {
                    Thread.sleep(1000); //Wait until we're unpaused...
                }
                Thread.sleep(1000); //Wait a tick as to not spam the PC
                output.writeByte(0);
                output.flush(); //Doesn't matter what is sent. Just testing the connection is live.
                return CONNECTION_ONGOING;
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                return CONNECTION_LOST;
            }
            catch (IOException e)
            {
                return CONNECTION_LOST;
            }
            catch (NullPointerException e)
            {
                //The IP has been changed.
                return CONNECTION_LOST;
            }
        }
    }

    @Override
    protected void onPostExecute(Byte result)
    {
        if (result.equals(CONNECTION_ESTABLISHED))
        {
            System.out.println("Connection established.");
            mainActivity.setConnectedView(true);
        }
        else if (result.equals(CONNECTION_LOST))
        {
            System.out.println("Connection lost.");
            connection = null;
            mainActivity.setConnectedView(false);
        }
        else if (result.equals(CONNECTION_ONGOING))
        {
            //This is fine. We don't have to do anything special here.
        }
        else
        {
            //¯\_(ツ)_/¯
            System.out.println("Connection entered unknown state.");
        }
        new ConnectionManager().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //Here we go again!
    }
    private boolean connect()
    {
        try
        {
            connection = new Socket();
            System.out.println("Connecting to "+MainActivity.getIP());
            connection.connect(new InetSocketAddress(MainActivity.getIP(), Integer.parseInt(MainActivity.getPort())), 3000);
            System.out.println("Connected to "+MainActivity.getIP());
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            byte connectionCode = input.readByte();
            if (connectionCode == CONNECTION_ESTABLISHED)
            {
                return true;
            }
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                if (connection != null)
                {
                    if (!connection.isClosed())
                    {
                        connection.close();
                    }
                    connection = null;
                }
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            return false;
        }
        catch (NullPointerException e)
        {
            //The IP has been changed.
            return false;
        }
    }
}
