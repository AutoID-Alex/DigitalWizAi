package ro.terapiabowentm.programari;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent){ LeadCheckReceiver.scheduleSoon(context); }
}
