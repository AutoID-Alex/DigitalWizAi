package ro.terapiabowentm.programari;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final ExecutorService ex=Executors.newSingleThreadExecutor();
    private final List<ApiClient.Booking> allItems=new ArrayList<>();
    private final List<ApiClient.Booking> filteredItems=new ArrayList<>();
    private final List<ApiClient.Booking> shownItems=new ArrayList<>();
    private BookingAdapter adapter;
    private ListView list;
    private ProgressBar progress;
    private EditText search;
    private TextView resultsInfo, emptyView;
    private Button loadMore;
    private int visibleLimit=10;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(23,63,44));
        buildUi();
        requestNotificationPermission();
        LeadCheckReceiver.scheduleSoon(this);
        if(ApiClient.token(this).isEmpty()) showConfig();
    }

    @Override protected void onResume(){
        super.onResume();
        if(!ApiClient.token(this).isEmpty()) load();
    }

    private void buildUi(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247,245,240));

        // Brand header - same visual language as the website.
        LinearLayout brand=new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setPadding(dp(18),dp(12),dp(18),dp(12));
        brand.setBackgroundColor(Color.WHITE);

        ImageView logo=new ImageView(this);
        logo.setImageResource(R.drawable.splash_logo_real);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        brand.addView(logo,new LinearLayout.LayoutParams(dp(52),dp(52)));

        LinearLayout brandText=new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(12),0,0,0);
        TextView siteName=text("Terapia Bowen Timișoara",20,true,Color.rgb(23,35,31));
        TextView owner=text("Denisa Jigmond · Programări",13,false,Color.rgb(53,107,92));
        brandText.addView(siteName);
        brandText.addView(owner);
        brand.addView(brandText,new LinearLayout.LayoutParams(0,dp(54),1));
        root.addView(brand);

        View accent=new View(this);
        accent.setBackgroundColor(Color.rgb(53,107,92));
        root.addView(accent,new LinearLayout.LayoutParams(-1,dp(3)));

        LinearLayout toolbar=new LinearLayout(this);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(18),dp(14),dp(12),dp(8));
        TextView title=text("Programări",27,true,Color.rgb(23,35,31));
        toolbar.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        toolbar.addView(actionButton("＋","Programare nouă",v->startActivity(new Intent(this,NewBookingActivity.class))));
        toolbar.addView(actionButton("↻","Reîncarcă",v->load()));
        toolbar.addView(actionButton("⚙","Setări",v->showConfig()));
        root.addView(toolbar);

        LinearLayout searchWrap=new LinearLayout(this);
        searchWrap.setPadding(dp(16),dp(4),dp(16),dp(6));
        search=new EditText(this);
        search.setSingleLine(true);
        search.setHint("Caută nume, telefon, email sau serviciu…");
        search.setTextSize(15);
        search.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search,0,0,0);
        search.setCompoundDrawablePadding(dp(8));
        search.setPadding(dp(14),0,dp(14),0);
        GradientDrawable searchBg=new GradientDrawable();
        searchBg.setColor(Color.WHITE);
        searchBg.setCornerRadius(dp(16));
        searchBg.setStroke(dp(1),Color.rgb(218,225,221));
        search.setBackground(searchBg);
        searchWrap.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        root.addView(searchWrap);

        LinearLayout infoRow=new LinearLayout(this);
        infoRow.setGravity(Gravity.CENTER_VERTICAL);
        infoRow.setPadding(dp(18),0,dp(18),dp(6));
        resultsInfo=text("Se încarcă…",13,false,Color.rgb(102,115,109));
        infoRow.addView(resultsInfo,new LinearLayout.LayoutParams(0,dp(30),1));
        root.addView(infoRow);

        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        root.addView(progress,new LinearLayout.LayoutParams(-1,dp(3)));

        list=new ListView(this);
        list.setDivider(null);
        list.setDividerHeight(0);
        list.setClipToPadding(false);
        list.setPadding(dp(12),dp(2),dp(12),dp(6));
        adapter=new BookingAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener((p,v,pos,id)->{
            Intent i=new Intent(this,BookingDetailActivity.class);
            i.putExtra("booking_id",shownItems.get(pos).id);
            startActivity(i);
        });

        emptyView=text("Nu am găsit programări.",15,false,Color.rgb(102,115,109));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setVisibility(View.GONE);
        root.addView(emptyView,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1));

        loadMore=new Button(this);
        loadMore.setText("ÎNCARCĂ ÎNCĂ 10");
        loadMore.setTextColor(Color.WHITE);
        loadMore.setTextSize(14);
        loadMore.setTypeface(null,Typeface.BOLD);
        loadMore.setAllCaps(false);
        GradientDrawable moreBg=new GradientDrawable();
        moreBg.setColor(Color.rgb(53,107,92));
        moreBg.setCornerRadius(dp(14));
        loadMore.setBackground(moreBg);
        loadMore.setOnClickListener(v->{ visibleLimit+=10; rebuildShown(); });
        LinearLayout.LayoutParams moreLp=new LinearLayout.LayoutParams(-1,dp(52));
        moreLp.setMargins(dp(18),dp(6),dp(18),dp(14));
        root.addView(loadMore,moreLp);

        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){ applyFilter(s.toString()); }
            public void afterTextChanged(Editable e){}
        });

        setContentView(root);
    }

    private View actionButton(String label,String description,View.OnClickListener click){
        TextView b=text(label,22,true,Color.rgb(53,107,92));
        b.setGravity(Gravity.CENTER);
        b.setContentDescription(description);
        b.setOnClickListener(click);
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1),Color.rgb(218,225,221));
        b.setBackground(bg);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(48),dp(44));
        lp.leftMargin=dp(7);
        b.setLayoutParams(lp);
        return b;
    }

    private void load(){
        if(ApiClient.token(this).isEmpty()) return;
        progress.setVisibility(View.VISIBLE);
        ex.execute(()->{
            try{
                List<ApiClient.Booking> r=ApiClient.list(this,200,0);
                runOnUiThread(()->{
                    allItems.clear();
                    allItems.addAll(r);
                    visibleLimit=10;
                    applyFilter(search.getText().toString());
                    progress.setVisibility(View.GONE);
                    android.content.SharedPreferences p=getSharedPreferences(ApiClient.PREFS,0);
                    if(!p.getBoolean("notifications_initialized",false)){
                        int m=0;
                        for(ApiClient.Booking b:r)m=Math.max(m,b.id);
                        p.edit().putInt("last_seen",m).putBoolean("notifications_initialized",true).apply();
                    }
                });
            }catch(Exception e){
                runOnUiThread(()->{
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this,"Eroare: "+e.getMessage(),Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void applyFilter(String raw){
        String q=raw==null?"":raw.trim().toLowerCase(Locale.ROOT);
        filteredItems.clear();
        for(ApiClient.Booking b:allItems){
            String hay=(safe(b.name)+" "+safe(b.phone)+" "+safe(b.email)+" "+safe(b.service)+" "+safe(b.statusLabel)+" "+safe(b.date)+" "+safe(b.time)).toLowerCase(Locale.ROOT);
            if(q.isEmpty()||hay.contains(q)) filteredItems.add(b);
        }
        visibleLimit=10;
        rebuildShown();
    }

    private void rebuildShown(){
        shownItems.clear();
        int end=Math.min(visibleLimit,filteredItems.size());
        for(int i=0;i<end;i++) shownItems.add(filteredItems.get(i));
        adapter.notifyDataSetChanged();
        boolean empty=filteredItems.isEmpty();
        emptyView.setVisibility(empty?View.VISIBLE:View.GONE);
        list.setVisibility(empty?View.GONE:View.VISIBLE);
        loadMore.setVisibility(!empty && end<filteredItems.size()?View.VISIBLE:View.GONE);
        if(empty) resultsInfo.setText("0 programări");
        else resultsInfo.setText("Afișate "+end+" din "+filteredItems.size()+" programări");
    }

    private String safe(String s){ return s==null?"":s; }

    private void showConfig(){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20),dp(6),dp(20),0);
        EditText api=new EditText(this); api.setHint("API URL"); api.setText(ApiClient.base(this)); box.addView(api);
        EditText tok=new EditText(this); tok.setHint("Token API"); tok.setText(ApiClient.token(this)); tok.setSingleLine(false); box.addView(tok);
        TextView info=text("Notificările pentru leaduri sunt verificate automat în background. Folosește testul pentru a verifica imediat canalul Android.",13,false,Color.rgb(102,115,109));
        info.setPadding(0,dp(12),0,0); box.addView(info);
        AlertDialog dlg=new AlertDialog.Builder(this)
            .setTitle("Conectare WordPress")
            .setMessage("Copiază URL-ul și tokenul din Settings → Terapia Bowen Site → Aplicație Android / API programări.")
            .setView(box)
            .setPositiveButton("Salvează",(d,w)->{
                ApiClient.save(this,api.getText().toString().trim(),tok.getText().toString().trim());
                LeadCheckReceiver.scheduleSoon(this);
                load();
            })
            .setNeutralButton("Testează notificarea",null)
            .setNegativeButton("Anulează",null)
            .create();
        dlg.setOnShowListener(x->dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{
            LeadCheckReceiver.showTestNotification(this);
            Toast.makeText(this,"Am trimis notificarea de test.",Toast.LENGTH_SHORT).show();
        }));
        dlg.show();
    }

    private class BookingAdapter extends BaseAdapter {
        public int getCount(){ return shownItems.size(); }
        public Object getItem(int position){ return shownItems.get(position); }
        public long getItemId(int position){ return shownItems.get(position).id; }
        public View getView(int position,View convertView,ViewGroup parent){
            ApiClient.Booking b=shownItems.get(position);
            FrameLayout outer=new FrameLayout(MainActivity.this);
            outer.setPadding(dp(4),dp(5),dp(4),dp(5));

            LinearLayout card=new LinearLayout(MainActivity.this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16),dp(14),dp(16),dp(14));
            GradientDrawable bg=new GradientDrawable();
            bg.setColor(Color.WHITE);
            bg.setCornerRadius(dp(18));
            bg.setStroke(dp(1),Color.rgb(230,234,232));
            card.setBackground(bg);

            LinearLayout line1=new LinearLayout(MainActivity.this);
            line1.setGravity(Gravity.CENTER_VERTICAL);
            TextView name=text(b.name.isEmpty()?"Fără nume":b.name,18,true,Color.rgb(23,35,31));
            line1.addView(name,new LinearLayout.LayoutParams(0,dp(34),1));
            TextView badge=text(b.statusLabel,12,true,Color.WHITE);
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dp(10),0,dp(10),0);
            GradientDrawable badgeBg=new GradientDrawable();
            badgeBg.setColor(statusColor(b.status));
            badgeBg.setCornerRadius(dp(14));
            badge.setBackground(badgeBg);
            line1.addView(badge,new LinearLayout.LayoutParams(-2,dp(28)));
            card.addView(line1);

            TextView service=text(b.service.isEmpty()?"Serviciu nesetat":b.service,15,true,Color.rgb(53,107,92));
            service.setPadding(0,dp(3),0,dp(5));
            card.addView(service);

            String schedule=b.schedule();
            TextView when=text("◷  "+schedule,14,false,Color.rgb(82,95,88));
            card.addView(when);

            if(!b.phone.isEmpty()){
                TextView phone=text("☎  "+b.phone,14,false,Color.rgb(82,95,88));
                phone.setPadding(0,dp(3),0,0);
                card.addView(phone);
            }

            outer.addView(card,new FrameLayout.LayoutParams(-1,-2));
            return outer;
        }
    }

    private int statusColor(String status){
        if("new".equals(status)) return Color.rgb(166,95,67);
        if("contacted".equals(status)) return Color.rgb(70,108,138);
        if("confirmed".equals(status)) return Color.rgb(53,107,92);
        if("completed".equals(status)) return Color.rgb(92,112,94);
        if("cancelled".equals(status)) return Color.rgb(140,76,76);
        if("no_show".equals(status)) return Color.rgb(110,110,110);
        return Color.rgb(102,115,109);
    }

    private TextView text(String s,int size,boolean bold,int color){
        TextView v=new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        if(bold)v.setTypeface(null,Typeface.BOLD);
        return v;
    }

    private void requestNotificationPermission(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},500);
    }

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
