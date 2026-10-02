package com.fari.maktabeh;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_SPEECH=91, REQ_AUDIO=90, REQ_IMPORT=92;
    private LinearLayout body;
    private SharedPreferences prefs;
    private int fontSize;
    private final ArrayList<Book> books=new ArrayList<>();

    static class Book {
        String title, category, author, text;
        boolean imported;
        Book(String t,String c,String a,String x){this(t,c,a,x,false);}
        Book(String t,String c,String a,String x,boolean i){title=t;category=c;author=a;text=x;imported=i;}
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("maktabeh",MODE_PRIVATE);
        fontSize=prefs.getInt("font_size",18);
        seed();
        loadImported();
        buildShell();
        showHome();
    }

    private void seed(){
        books.clear();
        books.add(new Book("قرآن کریم","قرآن","منبع قرآنی","نسخه کامل متن قرآن را فقط از نسخه آزاد یا دارای مجوز وارد کنید."));
        books.add(new Book("نهج البلاغه","شیعه","امام علی(ع)","کتابخانه نهج‌البلاغه؛ متن کامل در نسخه دارای مجوز/منبع آزاد قابل افزودن است."));
        books.add(new Book("صحیفه سجادیه","شیعه","امام سجاد(ع)","کتابخانه صحیفه سجادیه؛ متن کامل در نسخه دارای مجوز/منبع آزاد قابل افزودن است."));
        books.add(new Book("کتابخانه حدیث","حدیث","منابع حدیثی","جستجو بر اساس نام کتاب، باب، جلد، صفحه و منبع اصلی."));
        books.add(new Book("فقه و احکام","فقه","منابع معتبر","مطالب فقهی همراه با نام مرجع و ارجاع دقیق منبع."));
        books.add(new Book("تاریخ اسلام","تاریخ","منابع تاریخی","مطالعه تاریخ اسلام با ثبت مشخصات و ارجاع منابع."));
        books.add(new Book("علوم و دانش","دانش","منابع علمی","منابع علمی مجاز همراه با مشخصات کتابشناختی و لینک منبع اصلی."));
    }

    private void loadImported(){
        int n=prefs.getInt("import_count",0);
        for(int i=0;i<n;i++){
            String t=prefs.getString("import_title_"+i,null);
            String x=prefs.getString("import_text_"+i,null);
            if(t!=null && x!=null) books.add(new Book(t,"واردشده","فایل کاربر",x,true));
        }
    }

    private void buildShell(){
        ScrollView s=new ScrollView(this);
        body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(24,24,24,24);
        body.setTextDirection(View.TEXT_DIRECTION_RTL);
        s.addView(body);
        setContentView(s);
    }

    private TextView title(String s,int z){
        TextView v=new TextView(this); v.setText(s); v.setTextSize(z);
        v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setGravity(Gravity.RIGHT);
        v.setPadding(8,14,8,14); return v;
    }
    private Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(16);return b;}
    private void clear(){body.removeAllViews();}
    private void back(){Button b=btn("بازگشت");b.setOnClickListener(v->showHome());body.addView(b);}

    private void showHome(){
        clear();
        body.addView(title("مکتبه کامله فارسی — نسخه ۱.۰.۱",27));
        TextView sub=new TextView(this); sub.setText("کتابخانه | جستجو | علاقه‌مندی | یادداشت | گفتار به متن"); sub.setGravity(Gravity.RIGHT); body.addView(sub);
        String last=prefs.getString("last_book","");
        if(!last.isEmpty()){
            Button cont=btn("ادامه مطالعه: "+last);
            cont.setOnClickListener(v->{Book b=findBook(last); if(b!=null) showBook(b);});
            body.addView(cont);
        }
        EditText q=new EditText(this);q.setHint("جستجو در کتابخانه...");q.setSingleLine(true);q.setTextDirection(View.TEXT_DIRECTION_RTL);body.addView(q);
        Button search=btn("جستجو");search.setOnClickListener(v->doSearch(q.getText().toString()));body.addView(search);

        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);
        Button l=btn("کتابخانه"),c=btn("دسته‌ها"),f=btn("علاقه‌مندی");
        r.addView(l,new LinearLayout.LayoutParams(0,-2,1));r.addView(c,new LinearLayout.LayoutParams(0,-2,1));r.addView(f,new LinearLayout.LayoutParams(0,-2,1));body.addView(r);
        l.setOnClickListener(v->showLibrary("همه"));c.setOnClickListener(v->showCategories());f.setOnClickListener(v->showFavorites());

        LinearLayout r2=new LinearLayout(this);r2.setOrientation(LinearLayout.HORIZONTAL);
        Button n=btn("یادداشت‌ها"),i=btn("واردکردن TXT"),st=btn("تنظیمات");
        r2.addView(n,new LinearLayout.LayoutParams(0,-2,1));r2.addView(i,new LinearLayout.LayoutParams(0,-2,1));r2.addView(st,new LinearLayout.LayoutParams(0,-2,1));body.addView(r2);
        n.setOnClickListener(v->showNotes());i.setOnClickListener(v->importText());st.setOnClickListener(v->showSettings());

        body.addView(title("امکانات",21));
        Button speech=btn("تبدیل گفتار به متن فارسی");speech.setOnClickListener(v->startSpeech());body.addView(speech);
        Button share=btn("اشتراک‌گذاری برنامه");share.setOnClickListener(v->share("مکتبه کامله فارسی — کتابخانه فارسی آفلاین"));body.addView(share);
        Button about=btn("درباره و حقوق نشر");about.setOnClickListener(v->showAbout());body.addView(about);
        body.addView(title("منابع اولیه",21)); addBookCards(books);
    }

    private Book findBook(String name){for(Book b:books)if(b.title.equals(name))return b;return null;}

    private void addBookCards(List<Book> list){
        if(list.isEmpty()){TextView n=new TextView(this);n.setText("موردی پیدا نشد.");n.setGravity(Gravity.RIGHT);body.addView(n);return;}
        for(Book x:list){
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(12,8,12,8);
            card.addView(title(x.title,19));
            TextView a=new TextView(this);a.setText(x.category+" | "+x.author);a.setGravity(Gravity.RIGHT);card.addView(a);
            Button o=btn("مطالعه");o.setOnClickListener(v->showBook(x));card.addView(o);
            if(x.imported){Button del=btn("حذف فایل واردشده");del.setOnClickListener(v->deleteImported(x));card.addView(del);}
            body.addView(card);
        }
    }

    private void showLibrary(String cat){clear();body.addView(title("کتابخانه",26));back();addBookCards(cat.equals("همه")?books:filter(cat));}
    private ArrayList<Book> filter(String c){ArrayList<Book> r=new ArrayList<>();for(Book b:books)if(b.category.equals(c))r.add(b);return r;}

    private void showCategories(){
        clear();body.addView(title("دسته‌بندی منابع",26));back();
        LinkedHashSet<String>s=new LinkedHashSet<>();for(Book b:books)s.add(b.category);
        for(String c:s){Button x=btn(c);x.setOnClickListener(v->showLibrary(c));body.addView(x);}
    }

    private void doSearch(String q){
        q=q.trim(); if(q.isEmpty()){showLibrary("همه");return;}
        clear();body.addView(title("نتیجه جستجو: "+q,23));back();
        ArrayList<Book>r=new ArrayList<>();String z=q.toLowerCase(Locale.ROOT);
        for(Book b:books)if((b.title+b.category+b.author+b.text).toLowerCase(Locale.ROOT).contains(z))r.add(b);
        addBookCards(r);
    }

    private void showBook(Book b){
        clear();body.addView(title(b.title,fontSize+7));
        TextView m=new TextView(this);m.setText("دسته: "+b.category+"\nمنبع: "+b.author);m.setGravity(Gravity.RIGHT);body.addView(m);
        TextView t=new TextView(this);t.setText(b.text);t.setTextSize(fontSize);t.setGravity(Gravity.RIGHT);t.setPadding(8,20,8,20);body.addView(t);
        Button f=btn(prefs.getBoolean("fav_"+b.title,false)?"حذف از علاقه‌مندی‌ها":"افزودن به علاقه‌مندی‌ها");
        f.setOnClickListener(v->{boolean n=!prefs.getBoolean("fav_"+b.title,false);prefs.edit().putBoolean("fav_"+b.title,n).apply();f.setText(n?"حذف از علاقه‌مندی‌ها":"افزودن به علاقه‌مندی‌ها");});body.addView(f);
        Button no=btn("افزودن به یادداشت‌ها");no.setOnClickListener(v->appendNote(b.title+"\n"+b.text));body.addView(no);
        Button sh=btn("اشتراک‌گذاری متن");sh.setOnClickListener(v->share(b.title+"\n\n"+b.text));body.addView(sh);
        back();prefs.edit().putString("last_book",b.title).apply();
    }

    private void showFavorites(){
        clear();body.addView(title("علاقه‌مندی‌ها",26));back();
        ArrayList<Book>r=new ArrayList<>();for(Book b:books)if(prefs.getBoolean("fav_"+b.title,false))r.add(b);addBookCards(r);
    }

    private void showNotes(){
        clear();body.addView(title("یادداشت‌های من",26));back();
        EditText n=new EditText(this);n.setHint("یادداشت خود را بنویسید...");n.setGravity(Gravity.RIGHT|Gravity.TOP);n.setMinLines(10);n.setText(prefs.getString("note",""));n.setTextSize(fontSize);body.addView(n);
        Button s=btn("ذخیره");s.setOnClickListener(v->{prefs.edit().putString("note",n.getText().toString()).apply();Toast.makeText(this,"یادداشت ذخیره شد",Toast.LENGTH_SHORT).show();});body.addView(s);
        Button sh=btn("اشتراک‌گذاری یادداشت");sh.setOnClickListener(v->share(n.getText().toString()));body.addView(sh);
    }

    private void appendNote(String x){String o=prefs.getString("note","");prefs.edit().putString("note",o+(o.isEmpty()?"":"\n\n")+x).apply();Toast.makeText(this,"به یادداشت‌ها اضافه شد",Toast.LENGTH_SHORT).show();}

    private void showSettings(){
        clear();body.addView(title("تنظیمات",26));back();
        TextView z=new TextView(this);z.setText("اندازه متن مطالعه: "+fontSize);z.setGravity(Gravity.RIGHT);body.addView(z);
        Button minus=btn("کوچک‌تر"),plus=btn("بزرگ‌تر");body.addView(minus);body.addView(plus);
        minus.setOnClickListener(v->{if(fontSize>14)fontSize-=2;prefs.edit().putInt("font_size",fontSize).apply();z.setText("اندازه متن مطالعه: "+fontSize);});
        plus.setOnClickListener(v->{if(fontSize<30)fontSize+=2;prefs.edit().putInt("font_size",fontSize).apply();z.setText("اندازه متن مطالعه: "+fontSize);});
        Button clearNotes=btn("پاک‌کردن همه یادداشت‌ها");clearNotes.setOnClickListener(v->{prefs.edit().remove("note").apply();Toast.makeText(this,"یادداشت‌ها پاک شد",Toast.LENGTH_SHORT).show();});body.addView(clearNotes);
    }

    private void showAbout(){
        clear();body.addView(title("درباره مکتبه کامله فارسی",25));
        TextView t=new TextView(this);t.setText("کتابخانه فارسی آفلاین با جستجو، مطالعه، علاقه‌مندی، یادداشت، ادامه مطالعه، واردکردن فایل متنی و گفتار به متن.\n\nمحتوای دارای حق‌نشر بدون مجوز داخل برنامه کپی نمی‌شود. متن‌های آزاد یا دارای مجوز می‌توانند به کتابخانه اضافه شوند.");t.setTextSize(17);t.setGravity(Gravity.RIGHT);body.addView(t);back();
    }

    private void importText(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/plain");startActivityForResult(i,REQ_IMPORT);
    }

    private String displayName(Uri uri){
        Cursor c=null;try{c=getContentResolver().query(uri,null,null,null,null);if(c!=null&&c.moveToFirst()){int k=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(k>=0)return c.getString(k);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return "فایل واردشده";
    }

    private void saveImported(String title,String text){
        int n=prefs.getInt("import_count",0);
        prefs.edit().putString("import_title_"+n,title).putString("import_text_"+n,text).putInt("import_count",n+1).apply();
    }

    private void deleteImported(Book b){
        int n=prefs.getInt("import_count",0);ArrayList<String> titles=new ArrayList<>(),texts=new ArrayList<>();
        for(int i=0;i<n;i++){String t=prefs.getString("import_title_"+i,"");String x=prefs.getString("import_text_"+i,"");if(!t.equals(b.title)||!x.equals(b.text)){titles.add(t);texts.add(x);}}
        SharedPreferences.Editor e=prefs.edit();for(int i=0;i<n;i++){e.remove("import_title_"+i);e.remove("import_text_"+i);}e.putInt("import_count",titles.size());for(int i=0;i<titles.size();i++){e.putString("import_title_"+i,titles.get(i));e.putString("import_text_"+i,texts.get(i));}e.apply();books.remove(b);showLibrary("همه");
    }

    private void startSpeech(){
        if(android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_AUDIO);return;}
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"fa-IR");i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        try{startActivityForResult(i,REQ_SPEECH);}catch(Exception e){Toast.makeText(this,"تشخیص گفتار در این دستگاه در دسترس نیست",Toast.LENGTH_LONG).show();}
    }

    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d);if(c!=RESULT_OK||d==null)return;
        if(r==REQ_SPEECH){ArrayList<String>x=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(x!=null&&!x.isEmpty()){appendNote(x.get(0));showNotes();}}
        else if(r==REQ_IMPORT){
            try{
                InputStream in=getContentResolver().openInputStream(d.getData());
                BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
                StringBuilder s=new StringBuilder();String line;while((line=br.readLine())!=null)s.append(line).append('\n');br.close();
                String name=displayName(d.getData());saveImported(name,s.toString());books.add(new Book(name,"واردشده","فایل کاربر",s.toString(),true));
                Toast.makeText(this,"فایل وارد شد و برای دفعات بعد ذخیره شد",Toast.LENGTH_LONG).show();showLibrary("همه");
            }catch(Exception e){Toast.makeText(this,"خواندن فایل انجام نشد",Toast.LENGTH_LONG).show();}
        }
    }

    private void share(String text){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"اشتراک‌گذاری"));}
}
