package com.zazsona.pcmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

public class CommandSender extends AsyncTask<Byte, Void, Byte>
{
    private Context context;
    private static final byte CONNECTION_LOST = -2;
    private static final byte COMMAND_EXECUTED_SUCCESSFULLY = -4;
    private static final byte COMMAND_NOT_FOUND = -5;
    private static final byte COMMAND_NOT_SUPPORTED_ON_OS = -6;

    public void setContext(Context context)
    {
        this.context = context;
    }
    private byte runCommand(byte cid)
    {
        try
        {
            ConnectionManager.getOutput().writeObject(MainActivity.encrypt("{\"cid\": \"" + cid + "\"}")); //TODO: Null check
            ConnectionManager.getOutput().flush();
            byte response = ConnectionManager.getInput().readByte();
            return response;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return CONNECTION_LOST;
        }
    }
    @Override
    protected Byte doInBackground(Byte... args)
    {
        return runCommand(args[0]);
    }

    @Override
    protected void onPostExecute(Byte result)
    {
        AlertDialog.Builder commandNotice = new AlertDialog.Builder(context);
        commandNotice.setTitle("Command Sent");
        commandNotice.setNeutralButton("Got it!", (dialog, which) -> dialog.dismiss());
        System.out.println(result);
        if (result.equals(COMMAND_EXECUTED_SUCCESSFULLY))
        {
            commandNotice.setMessage("Command executed successfully.");
        }
        else if (result.equals(COMMAND_NOT_FOUND))
        {
            commandNotice.setMessage("Command not recognised.");
        }
        else if (result.equals(COMMAND_NOT_SUPPORTED_ON_OS))
        {
            commandNotice.setMessage("That command is not currently supported for your PC's OS. Keep an eye on updates!");
        }
        else
        {
            commandNotice.setMessage("It appears the connection has been lost.");
        }
        commandNotice.show();
    }
}
