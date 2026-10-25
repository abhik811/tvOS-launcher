package com.tvos.launcher;

import android.content.*;
import android.content.pm.*;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowPackageManager;

final class DemoApps {
    static void install(Context context){
        ShadowPackageManager pm=Shadows.shadowOf(context.getPackageManager());
        for(String name:new String[]{"Cinema","Files","Music","Photos","Player","Studio","Weather","Radio"}){
            String id="example.fixture."+name.toLowerCase(java.util.Locale.ROOT);ComponentName component=new ComponentName(id,id+".Main");
            ActivityInfo info=pm.addActivityIfNotPresent(component);info.enabled=true;info.exported=true;info.nonLocalizedLabel=name;info.applicationInfo.enabled=true;info.applicationInfo.flags|=ApplicationInfo.FLAG_INSTALLED;
            IntentFilter filter=new IntentFilter(Intent.ACTION_MAIN);filter.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);pm.addIntentFilterForActivity(component,filter);
        }
    }
}
