package com.tvos.launcher;

import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import java.util.*;

final class AppCatalog {
    static final class Entry {
        final String id,label; final Intent launch; final ResolveInfo activity;
        Entry(String id,String label,Intent launch,ResolveInfo activity){this.id=id;this.label=label;this.launch=launch;this.activity=activity;}
    }
    static List<Entry> load(Context c,boolean includePhone){
        PackageManager pm=c.getPackageManager();Map<String,Entry> unique=new TreeMap<>();
        String[] categories=includePhone?new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER};
        for(String category:categories)for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)){
            String pkg=r.activityInfo.packageName;if(pkg.equals(c.getPackageName())||unique.containsKey(pkg))continue;
            Intent launch=pm.getLeanbackLaunchIntentForPackage(pkg);if(launch==null)launch=pm.getLaunchIntentForPackage(pkg);
            if(launch==null)continue;unique.put(pkg,new Entry(pkg,r.loadLabel(pm).toString(),launch,r));
        }
        List<Entry> sorted=new ArrayList<>(unique.values());sorted.sort(Comparator.comparing(e->e.label.toLowerCase(Locale.ROOT)));
        List<String> ids=new ArrayList<>();for(Entry e:sorted)ids.add(e.id);
        List<Entry> result=new ArrayList<>();for(String id:AppOrder.arrange(ids,new Preferences(c).order()))result.add(unique.get(id));return result;
    }
    static Bitmap artwork(Context c,Entry e,int w,int h){
        PackageManager pm=c.getPackageManager();Drawable drawable=null;
        try{drawable=e.activity.activityInfo.loadBanner(pm);}catch(RuntimeException ignored){}
        boolean banner=drawable!=null;
        boolean customIcon=e.activity.activityInfo.icon!=0 || e.activity.activityInfo.applicationInfo.icon!=0;
        if(drawable==null && customIcon)try{drawable=e.activity.loadIcon(pm);}catch(RuntimeException ignored){}
        Bitmap bitmap=Bitmap.createBitmap(Math.max(w,1),Math.max(h,1),Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        int hue=Math.floorMod(e.id.hashCode(),360);
        paint.setShader(new LinearGradient(0,0,w,h,Color.HSVToColor(new float[]{hue,.48f,.30f}),Color.HSVToColor(new float[]{hue,.50f,.14f}),Shader.TileMode.CLAMP));canvas.drawRect(0,0,w,h,paint);paint.setShader(null);
        if(drawable!=null){
            int dw=drawable.getIntrinsicWidth(),dh=drawable.getIntrinsicHeight();if(dw<=0||dh<=0){dw=dh=1;}
            float scale=banner?Math.max(w/(float)dw,h/(float)dh):Math.min(w*.78f/dw,h*.78f/dh);
            int width=Math.max(1,Math.round(dw*scale)),height=Math.max(1,Math.round(dh*scale));
            Rect old=new Rect(drawable.getBounds());drawable.setBounds((w-width)/2,(h-height)/2,(w+width)/2,(h+height)/2);drawable.draw(canvas);drawable.setBounds(old);
        }else{
            paint.setColor(Ui.WHITE);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextSize(h*.48f);paint.setTextAlign(Paint.Align.CENTER);
            String initial=e.label.isEmpty()?"?":e.label.substring(0,e.label.offsetByCodePoints(0,1)).toUpperCase(Locale.ROOT);
            canvas.drawText(initial,w/2f,h/2f-(paint.ascent()+paint.descent())/2f,paint);
        }
        return bitmap;
    }
}
