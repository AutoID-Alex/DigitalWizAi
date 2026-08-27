package ro.terapiabowentm.programari;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.view.*;
import android.widget.*;

public class SplashActivity extends Activity {
    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(23,63,44));
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(dp(40),dp(40),dp(40),dp(40)); root.setBackgroundColor(Color.rgb(23,63,44));
        ImageView icon=new ImageView(this); icon.setImageResource(R.drawable.splash_logo_real); icon.setScaleType(ImageView.ScaleType.FIT_CENTER); icon.setPadding(dp(18),dp(18),dp(18),dp(18)); GradientDrawable bg=new GradientDrawable(); bg.setColor(Color.rgb(247,245,240)); bg.setCornerRadius(dp(34)); icon.setBackground(bg); root.addView(icon,new LinearLayout.LayoutParams(dp(138),dp(138)));
        TextView t=new TextView(this); t.setText("Terapia Bowen\nProgramări"); t.setTextColor(Color.WHITE); t.setTextSize(26); t.setGravity(Gravity.CENTER); t.setPadding(0,dp(22),0,dp(22)); root.addView(t);
        ProgressBar p=new ProgressBar(this); root.addView(p,new LinearLayout.LayoutParams(dp(44),dp(44))); setContentView(root);
        new Handler(Looper.getMainLooper()).postDelayed(()->{startActivity(new Intent(this,MainActivity.class));finish();},750);
    }
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);} }
