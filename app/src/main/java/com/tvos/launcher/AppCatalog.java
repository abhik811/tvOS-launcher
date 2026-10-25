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
        Drawable d=null;try{d=e.activity.activityInfo.loadBanner(c.getPackageManager());}catch(RuntimeException ignored){}
        boolean banner=d!=null;if(d==null)try{d=e.activity.loadIcon(c.getPackageManager());}catch(RuntimeException ignored){}
        if(d==null)return null;
        Bitmap b=Bitmap.createBitmap(Math.max(w,1),Math.max(h,1),Bitmap.Config.RGB_565);Canvas canvas=new Canvas(b);canvas.drawColor(0xff24384e);
        int inset=banner?0:h/6;int width=banner?w:h-inset*2;
        Rect old=new Rect(d.getBounds());d.setBounds((w-width)/2,inset,(w+width)/2,h-inset);d.draw(canvas);d.setBounds(old);return b;
    }
}
