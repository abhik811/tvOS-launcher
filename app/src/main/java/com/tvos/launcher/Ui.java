package com.tvos.launcher;

import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

final class Ui {
    static final int INK=0xff102536, WHITE=0xfff0f8ff, MUTED=0xffbed1e2;
    static int dp(Context c,float value){return Math.round(c.getResources().getDisplayMetrics().density*value);}
    static TextView text(Context c,String value,int size,int color){TextView t=new TextView(c);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("kern");return t;}
    static void edgeToEdge(Activity a){a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    static ColorStateList textColors(){return new ColorStateList(new int[][]{{android.R.attr.state_focused},{android.R.attr.state_pressed},{}},new int[]{INK,INK,WHITE});}
    static boolean animate(Context c){
        try{return new Preferences(c).motion() && Settings.Global.getFloat(c.getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0;}
        catch(RuntimeException ignored){return false;}
    }
    static LinearLayout button(Context c,String title,String value,String symbol,boolean arrow,Runnable action){
        LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(c,15),dp(c,10),dp(c,15),dp(c,10));row.setMinimumHeight(dp(c,46));
        row.setFocusable(true);row.setFocusableInTouchMode(true);row.setClickable(true);row.setId(View.generateViewId());
        row.setBackground(new GlassDrawable(c.getResources().getDisplayMetrics().density,new Preferences(c).glass(),12));
        if(symbol!=null){SymbolView icon=new SymbolView(c,symbol);icon.setDuplicateParentStateEnabled(true);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(c,23),dp(c,23));ip.setMarginEnd(dp(c,12));row.addView(icon,ip);}
        TextView name=text(c,title,17,WHITE);name.setTextColor(textColors());name.setDuplicateParentStateEnabled(true);name.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        if(value!=null||arrow){TextView end=text(c,(value==null?"":value)+(arrow?"  ›":""),16,WHITE);end.setTextColor(textColors());end.setDuplicateParentStateEnabled(true);end.setMaxWidth(dp(c,155));end.setMaxLines(2);end.setEllipsize(TextUtils.TruncateAt.END);end.setPadding(dp(c,10),0,0,0);end.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(end);}
        row.setContentDescription(title+(value==null||value.isEmpty()?"":", "+value));row.setOnClickListener(v->action.run());
        return row;
    }
    static void append(LinearLayout rows,View row){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(rows.getContext(),6);rows.addView(row,p);}
    static void note(LinearLayout rows,String value){TextView t=text(rows.getContext(),value,16,MUTED);t.setPadding(dp(rows.getContext(),13),dp(rows.getContext(),10),dp(rows.getContext(),13),dp(rows.getContext(),10));rows.addView(t);}
    static void open(Context c,Intent intent){try{c.startActivity(intent);}catch(ActivityNotFoundException|SecurityException e){Toast.makeText(c,"This control is not available on this device.",Toast.LENGTH_LONG).show();}}
    static void system(Context c,String action){
        try{c.startActivity(new Intent(action));}catch(ActivityNotFoundException|SecurityException unavailable){open(c,new Intent(Settings.ACTION_SETTINGS));}
    }
    static void focus(View target){if(target!=null){target.requestFocus();target.post(target::requestFocus);}}
    static final class Page {
        final FrameLayout root;
        final LinearLayout rows;
        final TextView explanation;
        Page(Activity a,String title,String symbol,String subtitle){
            edgeToEdge(a);root=new FrameLayout(a);root.addView(new BackdropView(a),new FrameLayout.LayoutParams(-1,-1));
            View veil=new View(a);veil.setBackgroundColor(0x6a071423);root.addView(veil,new FrameLayout.LayoutParams(-1,-1));
            TextView heading=text(a,title,27,WHITE);heading.setGravity(Gravity.CENTER);heading.setId(R.id.page_title);if(Build.VERSION.SDK_INT>=28)heading.setAccessibilityHeading(true);
            FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,dp(a,60));hp.topMargin=dp(a,20);root.addView(heading,hp);
            LinearLayout content=new LinearLayout(a);content.setGravity(Gravity.CENTER_VERTICAL);content.setPadding(dp(a,60),0,dp(a,60),dp(a,24));
            FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-1);cp.topMargin=dp(a,94);root.addView(content,cp);
            LinearLayout aside=new LinearLayout(a);aside.setOrientation(LinearLayout.VERTICAL);aside.setGravity(Gravity.CENTER);aside.setPadding(0,0,dp(a,44),0);
            SymbolView icon=new SymbolView(a,symbol);aside.addView(icon,new LinearLayout.LayoutParams(dp(a,104),dp(a,104)));
            explanation=text(a,subtitle,16,MUTED);explanation.setGravity(Gravity.CENTER);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2);ep.topMargin=dp(a,28);aside.addView(explanation,ep);
            content.addView(aside,new LinearLayout.LayoutParams(0,-2,.49f));
            ScrollView scroll=new ScrollView(a);scroll.setClipToPadding(false);scroll.setPadding(dp(a,6),dp(a,5),dp(a,6),dp(a,8));scroll.setVerticalScrollBarEnabled(false);
            rows=new LinearLayout(a);rows.setId(R.id.page_rows);rows.setOrientation(LinearLayout.VERTICAL);scroll.addView(rows);content.addView(scroll,new LinearLayout.LayoutParams(0,-1,.51f));a.setContentView(root);
        }
        View row(String id,String label,String value,boolean arrow,Runnable action){View v=button(root.getContext(),label,value,null,arrow,action);v.setTag(id);append(rows,v);return v;}
        void info(String value){note(rows,value);}
        void value(String label,String value){
            LinearLayout line=new LinearLayout(root.getContext());line.setGravity(Gravity.CENTER_VERTICAL);line.setPadding(dp(root.getContext(),13),dp(root.getContext(),9),dp(root.getContext(),13),dp(root.getContext(),9));
            TextView l=text(root.getContext(),label,16,MUTED),v=text(root.getContext(),value,16,WHITE);v.setGravity(Gravity.END);
            line.addView(l,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(0,-2,1.3f);vp.setMarginStart(dp(root.getContext(),12));line.addView(v,vp);rows.addView(line);
        }
        void restore(String key){View v=key==null?null:rows.findViewWithTag(key);if(v==null)for(int i=0;i<rows.getChildCount();i++)if(rows.getChildAt(i).isFocusable()){v=rows.getChildAt(i);break;}focus(v);}
    }
}
