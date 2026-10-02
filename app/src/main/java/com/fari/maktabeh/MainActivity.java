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
    private int fontSize;
    static class Book { String title,category,author,text; Book(String t,String c,String a,String x){title=t;category=c;author=a;text=x;} }
    @Override public void onCreate(Bundle b){ super.onCreate(b); prefs=getSharedPreferences("maktabeh",MODE_PRIVATE); fontSize=prefs.getInt("font_size",18); seed(); buildShell(); showHome(); }
    private void seed(){
        books.clear();
        books.add(new Book("قرآن کریم","قرآن","منبع قرآنی","این بخش برای متن قرآن و جستجوی آیات طراحی شده است. متن کامل باید از منبع آزاد یا مجاز وارد شود."));
        books.add(new Book("نهج البلاغه","شیعه","امام علی(ع)","این بخش برای مطالعه نهج‌البلاغه و ثبت ارجاع دقیق منبع طراحی شده است."));
        books.add(new Book("صحیفه سجادیه","شیعه","امام سجاد(ع)","این بخش برای مطالعه صحیفه سجادیه و ثبت ارجاع دقیق منبع طراحی شده است."));
        books.add(new Book("کتابخانه حدیث","حدیث","منابع حدیثی","جستجو و مطالعه منابع حدیثی با نام کتاب، جلد، صفحه و پیوند منبع."));
        books.add(new Book("فقه و احکام","فقه","منابع معتبر","مطالب فقهی باید همراه با نام مرجع و منبع دقیق نمایش داده شوند."));
        books.add(new Book("تاریخ اسلام","تاریخ","منابع تاریخی","مطالعه تاریخی با امکان نگهداری نام و پیوند منابع."));
        books.add(new Book("علوم و دانش","دانش","منابع علمی","برای منابع علمی مجاز، عنوان، نویسنده و لینک منبع اصلی نگهداری می‌شود."));
    }
    private void buildShell(){ ScrollView s=new ScrollView(this); body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(24,24,24,24); body.setTextDirection(View.TEXT_DIRECTION_RTL); s.addView(body); setContentView(s); }
    private TextView title(String s,int z){ TextView v=new TextView(this); v.setText(s); v.setTextSize(z); v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setGravity(Gravity.RIGHT); v.setPadding(8,14,8,14); return v; }
    private Button btn(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(16); return b; }
    private void clear(){ body.removeAllViews(); }
    private void back(){ Button b=btn("بازگشت"); b.setOnClickListener(v->showHome()); body.addView(b); }
    private void showHome(){
        clear(); body.addView(title("مکتبه کامله فارسی — نسخه ۱.۰.۱",27));
        TextView sub=new TextView(this); sub.setText("کتابخانه فارسی | جستجو | علاقه‌مندی | یادداشت | گفتار به متن"); sub.setTextSize(16); sub.setGravity(Gravity.RIGHT); body.addView(sub);
        search=new EditText(this); search.setHint("جستجو در عنوان، دسته، نویسنده و متن..."); search.setSingleLine(true); search.setTextDirection(View.TEXT_DIRECTION_RTL); body.addView(search);
        Button go=btn("جستجو"); go.setOnClickListener(v->doSearch(search.getText().toString())); body.addView(go);
        LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); Button l=btn("کتابخانه"),c=btn("دسته‌ها"),f=btn("علاقه‌مندی"); r.addView(l,new LinearLayout.LayoutParams(0,-2,1)); r.addView(c,new LinearLayout.LayoutParams(0,-2,1)); r.addView(f,new LinearLayout.LayoutParams(0,-2,1)); body.addView(r); l.setOnClickListener(v->showLibrary("همه")); c.setOnClickListener(v->showCategories()); f.setOnClickListener(v->showFavorites());
        LinearLayout r2=new LinearLayout(this); r2.setOrientation(LinearLayout.HORIZONTAL); Button n=btn("یادداشت‌ها"),i=btn("واردکردن TXT"),st=btn("تنظیمات"); r2.addView(n,new LinearLayout.LayoutParams(0,-2,1)); r2.addView(i,new LinearLayout.LayoutParams(0,-2,1)); r2.addView(st,new LinearLayout.LayoutParams(0,-2,1)); body.addView(r2); n.setOnClickListener(v->showNotes()); i.setOnClickListener(v->importText()); st.setOnClickListener(v->showSettings());
        body.addView(title("امکانات",21)); Button speech=btn("تبدیل گفتار به متن فارسی"); speech.setOnClickListener(v->startSpeech()); body.addView(speech); Button share=btn("اشتراک‌گذاری برنامه"); share.setOnClickListener(v->share("مکتبه کامله فارسی")); body.addView(share); Button about=btn("درباره و حقوق نشر"); about.setOnClickListener(v->showAbout()); body.addView(about);
        body.addView(title("منابع اولیه",21)); addBookCards(books);
    }
    private void addBookCards(List<Book> list){ if(list.isEmpty()){TextView n=new TextView(this);n.setText("موردی پیدا نشد.");n.setGravity(Gravity.RIGHT);body.addView(n);return;} for(Book x:list){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);TextView t=title(x.title,19);card.addView(t);TextView a=new TextView(this);a.setText(x.category+" | "+x.author);a.setGravity(Gravity.RIGHT);card.addView(a);Button o=btn("مطالعه");o.setOnClickListener(v->showBook(x));card.addView(o);body.addView(card);}}
    private void showLibrary(String cat){clear();body.addView(title("کتابخانه",26));back();addBookCards(cat.equals("همه")?books:filter(cat));}
    private ArrayList<Book> filter(String c){ArrayList<Book> r=new ArrayList<>();for(Book b:books)if(b.category.equals(c))r.add(b);return r;}
    private void showCategories(){clear();body.addView(title("دسته‌بندی منابع",26));back();LinkedHashSet<String>s=new LinkedHashSet<>();for(Book b:books)s.add(b.category);for(String c:s){Button x=btn(c);x.setOnClickListener(v->showLibrary(c));body.addView(x);}}
    private void doSearch(String q){q=q.trim();if(q.isEmpty()){showLibrary("همه");return;}clear();body.addView(title("نتیجه جستجو: "+q,23));back();ArrayList<Book>r=new ArrayList<>();for(Book b:books)if((b.title+b.category+b.author+b.text).toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))r.add(b);addBookCards(r);}
    private void showBook(Book b){clear();body.addView(title(b.title,fontSize+7));TextView m=new TextView(this);m.setText("دسته: "+b.category+"\nنویسنده/منبع: "+b.author);m.setGravity(Gravity.RIGHT);body.addView(m);TextView t=new TextView(this);t.setText(b.text);t.setTextSize(fontSize);t.setGravity(Gravity.RIGHT);t.setPadding(8,20,8,20);body.addView(t);Button f=btn(prefs.getBoolean("fav_"+b.title,false)?"حذف از علاقه‌مندی‌ها":"افزودن به علاقه‌مندی‌ها");f.setOnClickListener(v->{boolean n=!prefs.getBoolean("fav_"+b.title,false);prefs.edit().putBoolean("fav_"+b.title,n).apply();f.setText(n?"حذف از علاقه‌مندی‌ها":"افزودن به علاقه‌مندی‌ها");});body.addView(f);Button sh=btn("اشتراک‌گذاری متن");sh.setOnClickListener(v->share(b.title+"\n\n"+b.text));body.addView(sh);Button no=btn("افزودن به یادداشت‌ها");no.setOnClickListener(v->appendNote(b.title+"\n"+b.text));body.addView(no);back();prefs.edit().putString("last_book",b.title).apply();}
    private void showFavorites(){clear();body.addView(title("علاقه‌مندی‌ها",26));back();ArrayList<Book>r=new ArrayList<>();for(Book b:books)if(prefs.getBoolean("fav_"+b.title,false))r.add(b);addBookCards(r);}
    private void showNotes(){clear();body.addView(title("یادداشت‌های من",26));back();EditText n=new EditText(this);n.setHint("یادداشت خود را بنویسید...");n.setGravity(Gravity.RIGHT|Gravity.TOP);n.setMinLines(10);n.setText(prefs.getString("note",""));n.setTextSize(fontSize);body.addView(n);Button s=btn("ذخیره");s.setOnClickListener(v->{prefs.edit().putString("note",n.getText().toString()).apply();Toast.makeText(this,"یادداشت ذخیره شد",Toast.LENGTH_SHORT).show();});body.addView(s);Button sh=btn("اشتراک‌گذاری یادداشت");sh.setOnClickListener(v->share(n.getText().toString()));body.addView(sh);}
    private void appendNote(String x){String o=prefs.getString("note","");prefs.edit().putString("note",o+(o.isEmpty()?"":"\n\n")+x).apply();Toast.makeText(this,"به یادداشت‌ها اضافه شد",Toast.LENGTH_SHORT).show();}
    private void showSettings(){clear();body.addView(title("تنظیمات",26));back();TextView z=new TextView(this);z.setText("اندازه متن مطالعه: "+fontSize);z.setGravity(Gravity.RIGHT);body.addView(z);Button minus=btn("کوچک‌تر"),plus=btn("بزرگ‌تر");body.addView(minus);body.addView(plus);minus.setOnClickListener(v->{if(fontSize>14)fontSize-=2;prefs.edit().putInt("font_size",fontSize).apply();z.setText("اندازه متن مطالعه: "+fontSize);});plus.setOnClickListener(v->{if(fontSize<30)fontSize+=2;prefs.edit().putInt("font_size",fontSize).apply();z.setText("اندازه متن مطالعه: "+fontSize);});}
    private void showAbout(){clear();body.addView(title("درباره مکتبه کامله فارسی",25));TextView t=new TextView(this);t.setText("کتابخانه فارسی آفلاین با جستجو، مطالعه، علاقه‌مندی، یادداشت، واردکردن فایل متنی و گفتار به متن.\n\nمحتوای دارای حق‌نشر بدون مجوز داخل برنامه کپی نمی‌شود؛ برای منابع مجاز می‌توان متن و ارجاع دقیق منبع را اضافه کرد.");t.setTextSize(17);t.setGravity(Gravity.RIGHT);body.addView(t);back();}
    private void importText(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/plain");startActivityForResult(i,92);}
    private void startSpeech(){if(android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},90);return;}Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"fa-IR");i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);try{startActivityForResult(i,91);}catch(Exception e){Toast.makeText(this,"تشخیص گفتار در این دستگاه در دسترس نیست",Toast.LENGTH_LONG).show();}}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d==null)return;if(r==91){ArrayList<String>x=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(x!=null&&!x.isEmpty()){appendNote(x.get(0));showNotes();}}else if(r==92){try{java.io.InputStream in=getContentResolver().openInputStream(d.getData());java.io.BufferedReader br=new java.io.BufferedReader(new java.io.InputStreamReader(in,"UTF-8"));StringBuilder s=new StringBuilder();String line;while((line=br.readLine())!=null)s.append(line).append('\n');br.close();books.add(new Book("فایل واردشده","واردشده","فایل کاربر",s.toString()));showLibrary("همه");}catch(Exception e){Toast.makeText(this,"خواندن فایل انجام نشد",Toast.LENGTH_LONG).show();}}}
    private void share(String text){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"اشتراک‌گذاری"));}
}
