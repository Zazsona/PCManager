package com.zazsona.pcmanager;

import android.app.AlertDialog;
import android.view.View;

public class ButtonManager implements View.OnClickListener
{
    private MainActivity mainActivity;
    private static final byte SHUTDOWN = 0;
    private static final byte STANDBY = 1;
    private static final byte LOCK = 2;
    private static final byte REBOOT = 3;

    public void setMainActivity(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }
    @Override
    public void onClick(View view)
    {
        int viewID = view.getId();
        CommandSender commandSender = new CommandSender();
        commandSender.setContext(mainActivity);
        switch (viewID)
        {
            case R.id.btnShutdown:
                commandSender.execute(SHUTDOWN);
                break;

            case R.id.btnStandby:
                commandSender.execute(STANDBY);
                break;

            case R.id.btnLock:
                commandSender.execute(LOCK);
                break;

            case R.id.btnRestart:
                commandSender.execute(REBOOT);
                break;

            case R.id.btnAbout:
                AlertDialog.Builder aboutBox = new AlertDialog.Builder(mainActivity);
                aboutBox.setTitle("About");
                aboutBox.setMessage("To get started, press \"New IP\" and enter the IP shown on your PC.");
                aboutBox.setNeutralButton("Ok!", (dialog, which) -> dialog.dismiss());
                aboutBox.show();
                break;

            case R.id.btnNCode:
                mainActivity.setNewIP();
                break;
        }
    }
}
