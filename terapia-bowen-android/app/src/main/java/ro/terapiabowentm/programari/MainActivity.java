package ro.terapiabowentm.programari;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final ExecutorService ex=Executors.newSingleThreadExecutor();
    private final List<ApiClient.Booking> items=new ArrayList<>();
    private ArrayAdapter<String> adapter; private ListView list; private ProgressBar progress;
    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(23,63,44)); buildUi(); requestNotificationPermission(); LeadCheckReceiver.scheduleSoon(this); if(ApiClient.token(this).isEmpty()) showConfig(); }
    @Override protected void onResume(){ super.onResume(); if(!ApiClient.token(this).isEmpty()) load(); }
    private void buildUi(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(247,245,240));
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(18),dp(14),dp(10),dp(10));
        TextView title=new TextView(this); title.setText("Programări"); title.setTextSize(28); title.setTextColor(Color.rgb(23,35,31)); title.setTypeface(null,1); top.addView(title,new LinearLayout.LayoutParams(0,dp(52),1));
        Button add=new Button(this); add.setText("＋"); add.setContentDescription("Programare nouă"); add.setTextSize(22); add.setOnClickListener(v->startActivity(new Intent(this,NewBookingActivity.class))); top.addView(add,new LinearLayout.LayoutParams(dp(58),dp(50)));
        Button refresh=new Button(this); refresh.setText("↻"); refresh.setContentDescription("Reîncarcă"); refresh.setOnClickListener(v->load()); top.addView(refresh,new LinearLayout.LayoutParams(dp(56),dp(50)));
        Button settings=new Button(this); settings.setText("⚙"); settings.setContentDescription("Setări API"); settings.setOnClickListener(v->showConfig()); top.addView(settings,new LinearLayout.LayoutParams(dp(56),dp(50))); root.addView(top);
        progress=new ProgressBar(this); progress.setVisibility(View.GONE); root.addView(progress,new LinearLayout.LayoutParams(-1,dp(4)));
        list=new ListView(this); list.setDividerHeight(1); adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_2,android.R.id.text1,new ArrayList<>()){ @Override public View getView(int p,View v,android.view.ViewGroup g){ View x=super.getView(p,v,g); TextView a=x.findViewById(android.R.id.text1),c=x.findViewById(android.R.id.text2); ApiClient.Booking b=items.get(p); a.setText(b.name+"  ·  "+b.statusLabel); a.setTextColor(Color.rgb(23,35,31)); a.setTextSize(17); c.setText((b.service.isEmpty()?"Serviciu nesetat":b.service)+"\n"+b.schedule()); c.setTextColor(Color.rgb(90,103,96)); c.setTextSize(14); x.setPadding(dp(16),dp(10),dp(16),dp(10)); x.setBackgroundColor(Color.WHITE); return x; }};
        list.setAdapter(adapter); list.setOnItemClickListener((p,v,pos,id)->{ Intent i=new Intent(this,BookingDetailActivity.class); i.putExtra("booking_id",items.get(pos).id); startActivity(i); }); root.addView(list,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }
    private void load(){ if(ApiClient.token(this).isEmpty()) return; progress.setVisibility(View.VISIBLE); ex.execute(()->{ try{ List<ApiClient.Booking> r=ApiClient.list(this,100,0); runOnUiThread(()->{items.clear();items.addAll(r); adapter.clear(); for(ApiClient.Booking b:r)adapter.add(b.name); adapter.notifyDataSetChanged(); progress.setVisibility(View.GONE); android.content.SharedPreferences p=getSharedPreferences(ApiClient.PREFS,0); if(!p.getBoolean("notifications_initialized",false)){ int m=0; for(ApiClient.Booking b:r)m=Math.max(m,b.id); p.edit().putInt("last_seen",m).putBoolean("notifications_initialized",true).apply(); }}); }catch(Exception e){ runOnUiThread(()->{progress.setVisibility(View.GONE);Toast.makeText(this,"Eroare: "+e.getMessage(),Toast.LENGTH_LONG).show();}); }}); }
    private void showConfig(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(6),dp(20),0);
        EditText api=new EditText(this); api.setHint("API URL"); api.setText(ApiClient.base(this)); box.addView(api);
        EditText tok=new EditText(this); tok.setHint("Token API"); tok.setText(ApiClient.token(this)); tok.setSingleLine(false); box.addView(tok);
        TextView info=new TextView(this); info.setText("Notificările pentru leaduri sunt verificate automat în background. Folosește butonul de test pentru a verifica imediat canalul Android."); info.setPadding(0,dp(12),0,0); box.addView(info);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Conectare WordPress").setMessage("Copiază URL-ul și tokenul din Settings → Terapia Bowen Site → Aplicație Android / API programări.").setView(box).setPositiveButton("Salvează",(d,w)->{ApiClient.save(this,api.getText().toString().trim(),tok.getText().toString().trim()); LeadCheckReceiver.scheduleSoon(this); load();}).setNeutralButton("Testează notificarea",null).setNegativeButton("Anulează",null).create();
        dlg.setOnShowListener(x->dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{ LeadCheckReceiver.showTestNotification(this); Toast.makeText(this,"Am trimis notificarea de test.",Toast.LENGTH_SHORT).show(); }));
        dlg.show();
    }
    private void requestNotificationPermission(){ if(Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},500); }
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);} }
