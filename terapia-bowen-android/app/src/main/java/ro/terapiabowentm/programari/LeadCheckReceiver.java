package ro.terapiabowentm.programari;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.util.*;

public class LeadCheckReceiver extends BroadcastReceiver {
    private static final long FIRST_DELAY_MS = 60 * 1000L;
    private static final long INTERVAL_MS = 10 * 60 * 1000L;
    private static final int REQUEST_CODE = 901;
    private static final String CHANNEL_ID = "tbm_leads";

    public static void schedule(Context c){ scheduleAt(c, INTERVAL_MS); }
    public static void scheduleSoon(Context c){ scheduleAt(c, FIRST_DELAY_MS); }

    private static void scheduleAt(Context c,long delay){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(c,LeadCheckReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(c,REQUEST_CODE,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        long at=android.os.SystemClock.elapsedRealtime()+delay;
        if(Build.VERSION.SDK_INT>=23) am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,at,pi);
        else am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,at,pi);
    }

    @Override public void onReceive(Context c,Intent i){
        final PendingResult pr=goAsync();
        new Thread(()->{
            try{
                if(ApiClient.token(c).isEmpty()) return;
                int last=c.getSharedPreferences(ApiClient.PREFS,0).getInt("last_seen",0);
                List<ApiClient.Booking> n=ApiClient.list(c,30,last);
                int max=last;
                for(ApiClient.Booking b:n) max=Math.max(max,b.id);
                if(last==0){
                    c.getSharedPreferences(ApiClient.PREFS,0).edit().putInt("last_seen",max).apply();
                    return;
                }
                for(ApiClient.Booking b:n){
                    if(b.id>last && "new".equals(b.status)) notifyLead(c,b);
                }
                if(max>last) c.getSharedPreferences(ApiClient.PREFS,0).edit().putInt("last_seen",max).apply();
            }catch(Exception ignored){
            }finally{
                schedule(c);
                pr.finish();
            }
        }).start();
    }

    public static void showTestNotification(Context c){
        NotificationManager nm=manager(c);
        Intent d=new Intent(c,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(c,9001,d,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder x=builder(c);
        x.setSmallIcon(R.drawable.ic_bowen)
         .setContentTitle("Notificările funcționează")
         .setContentText("Programări Bowen poate afișa alerte pentru leaduri noi.")
         .setAutoCancel(true)
         .setContentIntent(pi)
         .setCategory(Notification.CATEGORY_MESSAGE);
        if(Build.VERSION.SDK_INT<26) x.setPriority(Notification.PRIORITY_HIGH);
        nm.notify(9001,x.build());
    }

    private static void notifyLead(Context c,ApiClient.Booking b){
        NotificationManager nm=manager(c);
        Intent d=new Intent(c,BookingDetailActivity.class);
        d.putExtra("booking_id",b.id);
        PendingIntent pi=PendingIntent.getActivity(c,b.id,d,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder x=builder(c);
        x.setSmallIcon(R.drawable.ic_bowen)
         .setContentTitle("Lead nou · "+b.name)
         .setContentText(b.service.isEmpty()?"Programare nouă":b.service)
         .setStyle(new Notification.BigTextStyle().bigText((b.service.isEmpty()?"Programare nouă":b.service)+(b.phone.isEmpty()?"":" · "+b.phone)))
         .setAutoCancel(true)
         .setContentIntent(pi)
         .setCategory(Notification.CATEGORY_MESSAGE);
        if(Build.VERSION.SDK_INT<26) x.setPriority(Notification.PRIORITY_HIGH);
        nm.notify(10000+b.id,x.build());
    }

    private static NotificationManager manager(Context c){
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel ch=new NotificationChannel(CHANNEL_ID,"Leaduri noi",NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Notificări pentru solicitările noi de programare");
            ch.enableVibration(true);
            nm.createNotificationChannel(ch);
        }
        return nm;
    }

    private static Notification.Builder builder(Context c){
        return Build.VERSION.SDK_INT>=26?new Notification.Builder(c,CHANNEL_ID):new Notification.Builder(c);
    }
}
