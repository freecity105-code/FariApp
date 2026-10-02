package com.fari.maktabeh;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout body;
    private EditText search;
    private SharedPreferences prefs;
    private final ArrayList<Book> books = new ArrayList<>();

    static class Book {
        String title, category, author, text;
        Book(String t, String c, String a, String x){title=t;category=c;author=a;text=x;}
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("maktabeh",MODE_PRIVATE);
        seed(); buildShell(); showHome();
    }

    private void seed(){
        books.add(new Book("قرآن کریم","قرآن","منبع قرآنی","متن و جستجوی محتوای قرآنی در نسخه‌های بعدی به صورت منبع‌محور اضافه می‌شود."));
        books.add(new Book("نهج البلاغه","شیعه","امام علی(ع)","نهج‌البلاغه مجموعه‌ای از خطبه‌ها، نامه‌ها و حکمت‌هاست. این بخش برای مطالعه و جستجوی متن مجاز طراحی شده است."));
        books.add(new Book("صحیفه سجادیه","شیعه","امام سجاد(ع)","مجموعه دعاهای منسوب به امام سجاد(ع). متن کامل باید از منبع دارای مجوز وارد برنامه شود."));
        books.add(new Book("کتابخانه حدیث","حدیث","منابع حدیثی","این بخش برای گردآوری منابع حدیثی با ذکر دقیق کتاب، جلد، صفحه و پیوند منبع طراحی شده است."));
        books.add(new Book("فقه و احکام","فقه","منابع معتبر","مطالب فقهی باید همراه با نام مرجع، منبع و تاریخ آخرین بررسی نمایش داده شوند."));
        books.add(new Book("تاریخ اسلام","تاریخ","منابع تاریخی","مطالعه تاریخی با امکان مقایسه منابع و ثبت ارجاع‌ها."));
        books.add(new Book("علوم و دانش","دانش","منابع علمی","بخش علمی برای مقالات و کتاب‌های مجاز با لینک منبع اصلی و اطلاعات کتابشناختی."));
        books.add(new Book("یادداشت‌های من","شخصی","کاربر","یادداشت‌های شخصی شما روی گوشی ذخیره می‌شوند."));
    }

    private void buildShell(){
        ScrollView scroll=new ScrollView(this);
        body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(24,24,24,24); body.setTextDirection(View.TEXT_DIRECTION_RTL);
        scroll.addView(body); setContentView(scroll);
    }

    private TextView title(String s,int size){
        TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setGravity(Gravity.RIGHT); v.setPadding(8,14,8,14); return v;
    }
    private Button btn(String s){Button b=new Button(this); b.setText(s); b.setTextSize(16); return b;}
    private void clear(){body.removeAllViews();}

    private void showHome(){
        clear(); body.addView(title("مکتبه کامله فارسی",28));
        TextView sub=new TextView(this); sub.setText("کتابخانه فارسی | جستجو | یادداشت | گفتار به نوشتار"); sub.setTextSize(16); sub.setGravity(Gravity.RIGHT); body.addView(sub);
        search=new EditText(this); search.setHint("جستجو در کتابخانه..."); search.setSingleLine(true); search.setTextDirection(View.TEXT_DIRECTION_RTL); body.addView(search);
        Button go=btn("جستجو"); go.setOnClickListener(v->doSearch(search.getText().toString())); body.addView(go);
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER);
        Button library=btn("کتابخانه"); Button cats=btn("دسته‌ها"); Button notes=btn("یادداشت‌ها");
        row.addView(library,new LinearLayout.LayoutParams(0,-2,1)); row.addView(cats,new LinearLayout.LayoutParams(0,-2,1)); row.addView(notes,new LinearLayout.LayoutParams(0,-2,1)); body.addView(row);
        library.setOnClickListener(v->showLibrary("همه")); cats.setOnClickListener(v->showCategories()); notes.setOnClickListener(v->showNotes());
        body.addView(title("امکانات",21));
        Button speech=btn("تبدیل گفتار به متن"); speech.setOnClickListener(v->startSpeech()); body.addView(speech);
        Button telegram=btn("دریافت/اشتراک‌گذاری محتوای مجاز با تلگرام"); telegram.setOnClickListener(v->openUrl("https://telegram.org")); body.addView(telegram);
        Button whatsapp=btn("اشتراک‌گذاری با واتساپ"); whatsapp.setOnClickListener(v->share("مکتبه کامله فارسی")); body.addView(whatsapp);
        Button about=btn("درباره برنامه و حقوق نشر"); about.setOnClickListener(v->showAbout()); body.addView(about);
        body.addView(title("کتابخانه اولیه",21)); addBookCards(books);
    }

    private void addBookCards(List<Book> list){
        for(Book x:list){
            LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(12,8,12,8);
            TextView t=title(x.title,19); card.addView(t); TextView a=new TextView(this); a.setText(x.category+" | "+x.author); a.setGravity(Gravity.RIGHT); card.addView(a);
            Button open=btn("مطالعه"); open.setOnClickListener(v->showBook(x)); card.addView(open); body.addView(card);
        }
    }

    private void showLibrary(String cat){ clear(); body.addView(title("کتابخانه",26)); Button back=btn("بازگشت"); back.setOnClickListener(v->showHome()); body.addView(back); addBookCards(cat.equals("همه")?books:filter(cat)); }
    private ArrayList<Book> filter(String cat){ArrayList<Book> r=new ArrayList<>(); for(Book b:books) if(b.category.equals(cat)) r.add(b); return r;}

    private void showCategories(){
        clear(); body.addView(title("دسته‌بندی منابع",26)); Button back=btn("بازگشت"); back.setOnClickListener(v->showHome()); body.addView(back);
        LinkedHashSet<String> set=new LinkedHashSet<>(); for(Book b:books)set.add(b.category);
        for(String c:set){Button b=btn(c); b.setOnClickListener(v->showLibrary(c)); body.addView(b);}
    }

    private void doSearch(String q){
        if(q.trim().isEmpty()){showLibrary("همه");return;}
        clear(); body.addView(title("نتیجه جستجو: "+q,23)); Button back=btn("بازگشت"); back.setOnClickListener(v->showHome()); body.addView(back);
        ArrayList<Book> r=new ArrayList<>(); for(Book b:books) if((b.title+b.category+b.author+b.text).contains(q.trim())) r.add(b);
        if(r.isEmpty()){TextView n=new TextView(this);n.setText("نتیجه‌ای پیدا نشد. می‌توانید منبع جدید را بعداً با ذکر مرجع اضافه کنید.");n.setTextSize(17);n.setGravity(Gravity.RIGHT);body.addView(n);} else addBookCards(r);
    }

    private void showBook(Book b){
        clear(); body.addView(title(b.title,26)); TextView meta=new TextView(this); meta.setText("دسته: "+b.category+"\nنویسنده/منبع: "+b.author);meta.setGravity(Gravity.RIGHT);body.addView(meta);
        TextView text=new TextView(this); text.setText(b.text);text.setTextSize(18);text.setGravity(Gravity.RIGHT);text.setPadding(8,20,8,20);body.addView(text);
        Button fav=btn(prefs.getBoolean("fav_"+b.title,false)?"حذف از علاقه‌مندی‌ها":"افزودن به علاقه‌مندی‌ها"); fav.setOnClickListener(v->{boolean n=!prefs.getBoolean("fav_"+b.title,false);prefs.edit().putBoolean("fav_"+b.title,n).apply();fav.setText(n?"حذف از علاقه‌مندی‌ها":"افزودن به علاقه‌مندی‌ها");});body.addView(fav);
        Button share=btn("اشتراک‌گذاری");share.setOnClickListener(v->share(b.title+"\n\n"+b.text));body.addView(share);
        Button back=btn("بازگشت");back.setOnClickListener(v->showHome());body.addView(back);
    }

    private void showNotes(){
        clear(); body.addView(title("یادداشت‌های من",26)); Button back=btn("بازگشت");back.setOnClickListener(v->showHome());body.addView(back);
        EditText note=new EditText(this);note.setHint("یادداشت خود را بنویسید...");note.setGravity(Gravity.RIGHT|Gravity.TOP);note.setMinLines(8);note.setText(prefs.getString("note",""));body.addView(note);
        Button save=btn("ذخیره یادداشت");save.setOnClickListener(v->{prefs.edit().putString("note",note.getText().toString()).apply();Toast.makeText(this,"یادداشت ذخیره شد",Toast.LENGTH_SHORT).show();});body.addView(save);
    }

    private void showAbout(){
        clear();body.addView(title("درباره مکتبه کامله فارسی",25));TextView t=new TextView(this);t.setText("هدف برنامه: یک کتابخانه فارسی منبع‌محور با جستجو، مطالعه، یادداشت و تبدیل گفتار به متن.\n\nحقوق نشر: محتوای دارای حق‌نشر بدون مجوز داخل برنامه کپی نمی‌شود؛ برای منابع خارجی و کانال‌ها، لینک و ارجاع به منبع اصلی نگه‌داری می‌شود.\n\nاین نسخه هسته کاربردی برنامه است و معماری آن برای افزودن بانک‌های اطلاعاتی و منابع مجاز آماده شده است.");t.setTextSize(17);t.setGravity(Gravity.RIGHT);t.setPadding(8,10,8,20);body.addView(t);Button back=btn("بازگشت");back.setOnClickListener(v->showHome());body.addView(back);
    }

    private void startSpeech(){
        if(android.os.Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},90);return;}
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"fa-IR");i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_PROMPT,"صحبت کنید");try{startActivityForResult(i,91);}catch(Exception e){Toast.makeText(this,"تشخیص گفتار روی این دستگاه در دسترس نیست",Toast.LENGTH_LONG).show();}
    }
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==91&&c==RESULT_OK&&d!=null){ArrayList<String>x=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(x!=null&&!x.isEmpty()){showNotes(); EditText n=(EditText)body.getChildAt(2);n.append((n.length()>0?"\n":"")+x.get(0));}}}
    private void share(String text){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"اشتراک‌گذاری"));}
    private void openUrl(String u){try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u)));}catch(Exception ignored){}}
}
