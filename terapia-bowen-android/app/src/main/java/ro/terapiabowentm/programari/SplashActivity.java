package ro.terapiabowentm.programari;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.view.*;
import android.widget.*;

public class SplashActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(23,63,44));

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(36),dp(36),dp(36),dp(36));
        root.setBackgroundColor(Color.rgb(23,63,44));

        ImageView icon=new ImageView(this);
        icon.setImageResource(R.drawable.splash_logo_real);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(18),dp(18),dp(18),dp(18));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.rgb(247,245,240));
        bg.setCornerRadius(dp(36));
        icon.setBackground(bg);
        root.addView(icon,new LinearLayout.LayoutParams(dp(138),dp(138)));

        TextView brand=new TextView(this);
        brand.setText("Terapia Bowen Timișoara");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(25);
        brand.setTypeface(null,Typeface.BOLD);
        brand.setGravity(Gravity.CENTER);
        brand.setPadding(0,dp(22),0,dp(10));
        root.addView(brand);

        TextView greet=new TextView(this);
        greet.setText("Bună, Denisa!\nSpor la lucru și o zi cu programări frumoase. 🌿");
        greet.setTextColor(Color.rgb(235,244,239));
        greet.setTextSize(17);
        greet.setGravity(Gravity.CENTER);
        greet.setLineSpacing(0,1.15f);
        greet.setPadding(dp(10),0,dp(10),dp(24));
        root.addView(greet);

        ProgressBar p=new ProgressBar(this);
        root.addView(p,new LinearLayout.LayoutParams(dp(42),dp(42)));

        TextView loading=new TextView(this);
        loading.setText("Îți pregătesc programările…");
        loading.setTextColor(Color.rgb(204,224,214));
        loading.setTextSize(13);
        loading.setGravity(Gravity.CENTER);
        loading.setPadding(0,dp(12),0,0);
        root.addView(loading);

        setContentView(root);
        new Handler(Looper.getMainLooper()).postDelayed(()->{
            startActivity(new Intent(this,MainActivity.class));
            finish();
        },1350);
    }

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
