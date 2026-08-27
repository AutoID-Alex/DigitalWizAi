package ro.terapiabowentm.programari;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ApiClient {
    public static final String PREFS="tbm_app";
    public static String base(Context c){ return c.getSharedPreferences(PREFS,0).getString("api","https://terapiabowentm.ro/wp-json/tbm/v1/"); }
    public static String token(Context c){ return c.getSharedPreferences(PREFS,0).getString("token",""); }
    public static void save(Context c,String api,String token){ c.getSharedPreferences(PREFS,0).edit().putString("api",api.endsWith("/")?api:api+"/").putString("token",token.trim()).apply(); }
    private static String req(Context c,String method,String path,String body) throws Exception{
        HttpURLConnection h=(HttpURLConnection)new URL(base(c)+path).openConnection();
        h.setRequestMethod(method); h.setConnectTimeout(12000); h.setReadTimeout(15000);
        h.setRequestProperty("Authorization","Bearer "+token(c)); h.setRequestProperty("Accept","application/json");
        if(body!=null){ h.setDoOutput(true); h.setRequestProperty("Content-Type","application/json; charset=UTF-8"); try(OutputStream os=h.getOutputStream()){os.write(body.getBytes(StandardCharsets.UTF_8));} }
        int code=h.getResponseCode(); InputStream is=code>=200&&code<300?h.getInputStream():h.getErrorStream();
        String txt=""; if(is!=null) try(BufferedReader r=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))){String line;StringBuilder b=new StringBuilder();while((line=r.readLine())!=null)b.append(line);txt=b.toString();}
        if(code<200||code>=300) throw new IOException("HTTP "+code+" "+txt); return txt;
    }
    public static List<Booking> list(Context c,int limit,int since) throws Exception{
        JSONObject o=new JSONObject(req(c,"GET","bookings?limit="+limit+(since>0?"&since_id="+since:""),null)); JSONArray a=o.getJSONArray("items"); List<Booking> out=new ArrayList<>(); for(int i=0;i<a.length();i++) out.add(Booking.from(a.getJSONObject(i))); return out;
    }
    public static Booking get(Context c,int id)throws Exception{return Booking.from(new JSONObject(req(c,"GET","bookings/"+id,null)));}
    public static Booking update(Context c,Booking b)throws Exception{
        JSONObject j=new JSONObject(); j.put("status",b.status); j.put("date",b.date); j.put("time",b.time); j.put("location",b.location); j.put("cancellation_reason",b.reason);
        JSONObject o=new JSONObject(req(c,"PATCH","bookings/"+b.id,j.toString())); return Booking.from(o.getJSONObject("booking"));
    }
    public static class Booking{
        public int id; public String name="",phone="",email="",service="",message="",status="new",statusLabel="",date="",time="",location="",reason="",created="";
        static Booking from(JSONObject j){ Booking b=new Booking(); b.id=j.optInt("id"); b.name=j.optString("name"); b.phone=j.optString("phone"); b.email=j.optString("email"); b.service=j.optString("service"); b.message=j.optString("message"); b.status=j.optString("status","new"); b.statusLabel=j.optString("status_label",b.status); b.date=j.optString("date"); b.time=j.optString("time"); b.location=j.optString("location"); b.reason=j.optString("cancellation_reason"); b.created=j.optString("created_at"); return b; }
        public String schedule(){return date.isEmpty()?"Lead nou":date+(time.isEmpty()?"":" · "+time);} }
}
