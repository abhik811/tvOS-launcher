package com.tvos.launcher;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(sdk=34,qualifiers="w960dp-h540dp-land-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RenderingTest {
    @Test public void drawActualScreensWithNativeGraphics() throws Exception {
        new Preferences(RuntimeEnvironment.getApplication()).reset();
        try(ActivityController<SettingsActivity> c=Robolectric.buildActivity(SettingsActivity.class).setup().visible()){
            capture(c.get(),"settings");
            for(String tag:new String[]{"general","home","video","saver","remotes","access","system"}){
                c.get().getWindow().getDecorView().findViewWithTag(tag).performClick();capture(c.get(),"settings-"+tag);c.get().onBackPressed();
            }
        }
        for(String panel:new String[]{"network","audio","storage","display","apps","about","bluetooth"}){
            try(ActivityController<DeviceActivity> c=Robolectric.buildActivity(DeviceActivity.class,DeviceActivity.intent(RuntimeEnvironment.getApplication(),panel,null)).setup().visible()){capture(c.get(),panel);}
        }
        try(ActivityController<HomeActivity> c=Robolectric.buildActivity(HomeActivity.class).setup().visible()){capture(c.get(),"home-empty");}
        DemoApps.install(RuntimeEnvironment.getApplication());
        assertEquals(8,AppCatalog.load(RuntimeEnvironment.getApplication(),false).size());
        try(ActivityController<HomeActivity> c=Robolectric.buildActivity(HomeActivity.class).setup().visible()){capture(c.get(),"home-demo-apps");}
        try(ActivityController<ControlCenterActivity> c=Robolectric.buildActivity(ControlCenterActivity.class).setup().visible()){capture(c.get(),"control-center");}
    }
    private void capture(Activity activity,String name) throws IOException {
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().setInTouchMode(false);
        View root=activity.getWindow().getDecorView();int w=1920,h=1080;
        root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h);
        LinearLayout rows=activity.findViewById(R.id.page_rows);if(rows!=null)for(int n=0;n<rows.getChildCount();n++)if(rows.getChildAt(n).isFocusable()){rows.getChildAt(n).requestFocus();break;}
        Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);root.draw(new Canvas(b));Set<Integer> samples=new HashSet<>();for(int y=0;y<h;y+=50)for(int x=0;x<w;x+=50)samples.add(b.getPixel(x,y));assertTrue(samples.size()>12);
        String target=System.getProperty("tvos.renders");if(target!=null){File dir=new File(target);assertTrue(dir.isDirectory()||dir.mkdirs());try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}b.recycle();
    }
}
