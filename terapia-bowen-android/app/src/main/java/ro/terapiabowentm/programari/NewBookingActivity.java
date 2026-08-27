package ro.terapiabowentm.programari;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public class NewBookingActivity extends Activity {
    private final ExecutorService ex=Executors.newSingleThreadExecutor();
    private EditText name,phone,email,service,date,time,location,message,reason;
    private Spinner status; private CheckBox reviewConsent; private Button save;
    private final String[] labels={"Lead primit","Contactat telefonic","Confirmată","Finalizată","Anulată","Neprezentare"};
    private final String[] keys={"new","contacted","confirmed","completed","cancelled","no_show"};

    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(23,63,44)); buildUi(); }

    private void buildUi(){
        ScrollView scroll=new ScrollView(this); scroll.setBackgroundColor(Color.rgb(247,245,240));
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(18),dp(18),dp(28)); scroll.addView(root);
        TextView title=new TextView(this); title.setText("Programare nouă"); title.setTextSize(27); title.setTextColor(Color.rgb(23,35,31)); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); root.addView(title);
        TextView sub=new TextView(this); sub.setText("Adaugă o programare direct în sistem. Dacă o salvezi ca Confirmată, data și ora sunt obligatorii."); sub.setTextSize(14); sub.setTextColor(Color.rgb(90,103,96)); sub.setPadding(0,dp(6),0,dp(14)); root.addView(sub);
        name=field(root,"Nume client *",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        phone=field(root,"Telefon",InputType.TYPE_CLASS_PHONE);
        email=field(root,"Email",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        service=field(root,"Serviciu",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        addLabel(root,"Status"); status=new Spinner(this); status.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,labels)); root.addView(status,new LinearLayout.LayoutParams(-1,dp(52)));
        date=field(root,"Data (apasă pentru selectare)",InputType.TYPE_NULL); date.setFocusable(false); date.setOnClickListener(v->pickDate());
        time=field(root,"Ora (apasă pentru selectare)",InputType.TYPE_NULL); time.setFocusable(false); time.setOnClickListener(v->pickTime());
        location=field(root,"Locație (gol = adresa cabinetului)",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        message=field(root,"Notițe",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); message.setMinLines(3); message.setGravity(Gravity.TOP);
        reason=field(root,"Motiv anulare (opțional)",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); reason.setMinLines(2);
        reviewConsent=new CheckBox(this); reviewConsent.setText("Clientul acceptă solicitarea de feedback/review după ședință"); reviewConsent.setTextColor(Color.rgb(57,71,65)); reviewConsent.setPadding(0,dp(8),0,dp(10)); root.addView(reviewConsent);
        save=new Button(this); save.setText("Adaugă programarea"); save.setTextSize(16); save.setTextColor(Color.WHITE); save.setBackgroundColor(Color.rgb(53,107,92)); save.setOnClickListener(v->save()); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(56)); bp.topMargin=dp(12); root.addView(save,bp);
        Button cancel=new Button(this); cancel.setText("Renunță"); cancel.setOnClickListener(v->finish()); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(50)); cp.topMargin=dp(8); root.addView(cancel,cp);
        setContentView(scroll);
    }
    private EditText field(LinearLayout root,String hint,int input){ EditText e=new EditText(this); e.setHint(hint); e.setTextColor(Color.rgb(23,35,31)); e.setHintTextColor(Color.rgb(110,120,114)); e.setTextSize(16); e.setInputType(input); e.setPadding(dp(12),dp(10),dp(12),dp(10)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(10); root.addView(e,lp); return e; }
    private void addLabel(LinearLayout root,String text){ TextView l=new TextView(this); l.setText(text); l.setTextColor(Color.rgb(57,71,65)); l.setTextSize(13); l.setTypeface(Typeface.DEFAULT,Typeface.BOLD); l.setPadding(dp(2),dp(4),0,dp(2)); root.addView(l); }
    private void pickDate(){ Calendar c=Calendar.getInstance(); new DatePickerDialog(this,(v,y,m,d)->date.setText(String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show(); }
    private void pickTime(){ Calendar c=Calendar.getInstance(); new TimePickerDialog(this,(v,h,m)->time.setText(String.format(Locale.US,"%02d:%02d",h,m)),c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show(); }
    private void save(){
        String n=name.getText().toString().trim(), ph=phone.getText().toString().trim(), em=email.getText().toString().trim(); String st=keys[status.getSelectedItemPosition()];
        if(n.isEmpty()){ name.setError("Completează numele"); return; }
        if(ph.isEmpty()&&em.isEmpty()){ Toast.makeText(this,"Completează telefonul sau emailul.",Toast.LENGTH_LONG).show(); return; }
        if("confirmed".equals(st)&&(date.getText().toString().trim().isEmpty()||time.getText().toString().trim().isEmpty())){ Toast.makeText(this,"Pentru Confirmată completează data și ora.",Toast.LENGTH_LONG).show(); return; }
        ApiClient.Booking b=new ApiClient.Booking(); b.name=n; b.phone=ph; b.email=em; b.service=service.getText().toString().trim(); b.status=st; b.date=date.getText().toString().trim(); b.time=time.getText().toString().trim(); b.location=location.getText().toString().trim(); b.message=message.getText().toString().trim(); b.reason=reason.getText().toString().trim();
        save.setEnabled(false); save.setText("Se salvează...");
        ex.execute(()->{ try{ ApiClient.Booking created=ApiClient.create(this,b,reviewConsent.isChecked()); runOnUiThread(()->{ Toast.makeText(this,"Programarea a fost adăugată.",Toast.LENGTH_SHORT).show(); Intent data=new Intent(); data.putExtra("booking_id",created.id); setResult(RESULT_OK,data); finish(); }); }catch(Exception e){ runOnUiThread(()->{ save.setEnabled(true); save.setText("Adaugă programarea"); Toast.makeText(this,"Eroare: "+e.getMessage(),Toast.LENGTH_LONG).show(); }); }});
    }
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);} }
