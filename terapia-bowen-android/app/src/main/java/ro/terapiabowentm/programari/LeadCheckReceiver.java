package ro.terapiabowentm.programari;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.util.*;

public class LeadCheckReceiver extends BroadcastReceiver {
    public static void schedule(Context c){ AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE); Intent i=new Intent(c,LeadCheckReceiver.class); PendingIntent pi=PendingIntent.getBroadcast(c,901,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE); am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,android.os.SystemClock.elapsedRealtime()+15*60*1000,15*60*1000,pi); }
    @Override public void onReceive(Context c,Intent i){ final PendingResult pr=goAsync(); new Thread(()->{try{ if(ApiClient.token(c).isEmpty())return; int last=c.getSharedPreferences(ApiClient.PREFS,0).getInt("last_seen",0); List<ApiClient.Booking> n=ApiClient.list(c,30,last); int max=last; for(ApiClient.Booking b:n)max=Math.max(max,b.id); if(last==0){c.getSharedPreferences(ApiClient.PREFS,0).edit().putInt("last_seen",max).apply();return;} for(ApiClient.Booking b:n)notifyLead(c,b); if(max>last)c.getSharedPreferences(ApiClient.PREFS,0).edit().putInt("last_seen",max).apply();}catch(Exception ignored){}finally{pr.finish();}}).start(); }
    private void notifyLead(Context c,ApiClient.Booking b){ String ch="tbm_leads"; NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE); if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(ch,"Leaduri noi",NotificationManager.IMPORTANCE_HIGH)); Intent d=new Intent(c,BookingDetailActivity.class);d.putExtra("booking_id",b.id);PendingIntent pi=PendingIntent.getActivity(c,b.id,d,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE); Notification.Builder x=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,ch):new Notification.Builder(c); x.setSmallIcon(R.drawable.ic_bowen).setContentTitle("Lead nou · "+b.name).setContentText(b.service.isEmpty()?"Programare nouă":b.service).setAutoCancel(true).setContentIntent(pi); nm.notify(10000+b.id,x.build()); }
}
