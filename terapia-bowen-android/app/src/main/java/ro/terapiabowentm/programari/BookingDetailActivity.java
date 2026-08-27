package ro.terapiabowentm.programari;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.concurrent.*;

public class BookingDetailActivity extends Activity {
    private final ExecutorService ex=Executors.newSingleThreadExecutor(); private int id; private ApiClient.Booking b;
    private TextView title,contact,message; private Spinner status; private EditText date,time,location,reason; private Button save;
    private final String[] statusKeys={"new","contacted","confirmed","completed","cancelled","no_show"};
    private final String[] statusLabels={"Lead primit","Contactat telefonic","Confirmată","Finalizată","Anulată","Neprezentare"};
    @Override public void onCreate(Bundle x){super.onCreate(x);getWindow().setStatusBarColor(Color.rgb(23,63,44));id=getIntent().getIntExtra("booking_id",0);build();load();}
    private void build(){ ScrollView scroll=new ScrollView(this); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(20),dp(20),dp(34));root.setBackgroundColor(Color.rgb(247,245,240));scroll.addView(root);
        Button back=new Button(this);back.setText("← Înapoi");back.setOnClickListener(v->finish());root.addView(back,new LinearLayout.LayoutParams(-2,dp(48)));
        title=text("Programare",28,true);root.addView(title); contact=text("",15,false);root.addView(contact); message=text("",15,false);message.setPadding(0,dp(10),0,dp(14));root.addView(message);
        root.addView(label("Status")); status=new Spinner(this);status.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,statusLabels));root.addView(status,new LinearLayout.LayoutParams(-1,dp(56)));
        root.addView(label("Data")); date=new EditText(this);date.setHint("YYYY-MM-DD");date.setFocusable(false);date.setOnClickListener(v->pickDate());root.addView(date);
        root.addView(label("Ora")); time=new EditText(this);time.setHint("HH:MM");time.setFocusable(false);time.setOnClickListener(v->pickTime());root.addView(time);
        root.addView(label("Locație"));location=new EditText(this);root.addView(location);
        root.addView(label("Motiv anulare"));reason=new EditText(this);reason.setMinLines(2);root.addView(reason);
        save=new Button(this);save.setText("UPDATE PROGRAMARE");save.setTextColor(Color.WHITE);save.setBackgroundColor(Color.rgb(53,107,92));save.setOnClickListener(v->update());LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(58));sp.topMargin=dp(22);root.addView(save,sp);setContentView(scroll); }
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.rgb(23,35,31));if(bold)v.setTypeface(null,1);return v;}
    private TextView label(String s){TextView v=text(s,13,true);v.setPadding(0,dp(16),0,dp(4));return v;}
    private void load(){save.setEnabled(false);ex.execute(()->{try{ApiClient.Booking x=ApiClient.get(this,id);runOnUiThread(()->{b=x;title.setText(x.name);contact.setText((x.service.isEmpty()?"Serviciu nesetat":x.service)+"\n"+x.phone+(x.email.isEmpty()?"":" · "+x.email));message.setText(x.message);int p=0;for(int i=0;i<statusKeys.length;i++)if(statusKeys[i].equals(x.status))p=i;status.setSelection(p);date.setText(x.date);time.setText(x.time);location.setText(x.location);reason.setText(x.reason);save.setEnabled(true);});}catch(Exception e){runOnUiThread(()->{Toast.makeText(this,"Eroare: "+e.getMessage(),Toast.LENGTH_LONG).show();finish();});}});}
    private void update(){if(b==null)return;b.status=statusKeys[status.getSelectedItemPosition()];b.date=date.getText().toString().trim();b.time=time.getText().toString().trim();b.location=location.getText().toString().trim();b.reason=reason.getText().toString().trim();save.setEnabled(false);ex.execute(()->{try{ApiClient.update(this,b);runOnUiThread(()->{Toast.makeText(this,"Programarea a fost actualizată",Toast.LENGTH_SHORT).show();finish();});}catch(Exception e){runOnUiThread(()->{save.setEnabled(true);Toast.makeText(this,"Eroare: "+e.getMessage(),Toast.LENGTH_LONG).show();});}});}
    private void pickDate(){java.util.Calendar c=java.util.Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->date.setText(String.format(java.util.Locale.US,"%04d-%02d-%02d",y,m+1,d)),c.get(java.util.Calendar.YEAR),c.get(java.util.Calendar.MONTH),c.get(java.util.Calendar.DAY_OF_MONTH)).show();}
    private void pickTime(){java.util.Calendar c=java.util.Calendar.getInstance();new TimePickerDialog(this,(v,h,m)->time.setText(String.format(java.util.Locale.US,"%02d:%02d",h,m)),c.get(java.util.Calendar.HOUR_OF_DAY),c.get(java.util.Calendar.MINUTE),true).show();}
    private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density+.5f);} }
