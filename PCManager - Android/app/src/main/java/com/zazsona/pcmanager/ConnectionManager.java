package com.zazsona.pcmanager;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

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
                        Thread.sleep(2000);
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
                output.writeObject("0");
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
            DatagramSocket socket = new DatagramSocket();
            String linkToken = "foobar"; //TODO: Change to a token
            byte[] tokenBuffer = linkToken.getBytes();
            socket.setBroadcast(true);
            DatagramPacket broadcastPacket = new DatagramPacket(tokenBuffer, tokenBuffer.length, InetAddress.getByName("255.255.255.255"), 2866);
            socket.send(broadcastPacket);
            System.out.println("Packet sent.");

            byte[] returnedCode = new byte[linkToken.getBytes().length];
            DatagramPacket codePacket = new DatagramPacket(returnedCode, returnedCode.length);
            socket.receive(codePacket);

            //TODO: Check if registered device (May be superflous, server already does this check before responding)

            connection = new Socket();
            System.out.println("Connecting to "+codePacket.getAddress());
            connection.connect(new InetSocketAddress(codePacket.getAddress(), 2865), 3000);
            System.out.println("Connected to "+codePacket.getAddress());
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            byte connectionCode = input.readByte();
            if (connectionCode == CONNECTION_ESTABLISHED)
            {
                return true;
            }
            return false;
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
