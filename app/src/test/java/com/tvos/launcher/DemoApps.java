package com.tvos.launcher;

import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowPackageManager;

/** Preview-only apps and original banner fixtures; never included in the installed APK. */
final class DemoApps {
    static void install(Context context){
        ShadowPackageManager pm=Shadows.shadowOf(context.getPackageManager());
        for(String name:new String[]{"Cinema","Files","Music","Photos","Player","Studio","Weather","Radio"}){
            String id="example.fixture."+name.toLowerCase(java.util.Locale.ROOT);ComponentName component=new ComponentName(id,id+".Main");
            ActivityInfo info=pm.addActivityIfNotPresent(component);info.enabled=true;info.exported=true;info.nonLocalizedLabel=name;info.applicationInfo.enabled=true;info.applicationInfo.flags|=ApplicationInfo.FLAG_INSTALLED;
            info.banner=1;pm.addDrawableResolution(id,1,new DemoBanner(name));
            IntentFilter filter=new IntentFilter(Intent.ACTION_MAIN);filter.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);pm.addIntentFilterForActivity(component,filter);
        }
    }
    private static final class DemoBanner extends Drawable {
        final String name;final Paint p=new Paint(3);final Path path=new Path();
        DemoBanner(String name){this.name=name;}
        @Override public int getIntrinsicWidth(){return 320;}
        @Override public int getIntrinsicHeight(){return 192;}
        @Override public void draw(Canvas c){
            c.save();c.translate(getBounds().left,getBounds().top);c.scale(getBounds().width()/320f,getBounds().height()/192f);
            int upper=0xff26313d,lower=0xff080b10;
            switch(name){
                case "Music":upper=0xffff5275;lower=0xffeb2349;break;
                case "Files":upper=0xff4cbdff;lower=0xff0875d7;break;
                case "Photos":upper=0xffffffff;lower=0xfff2f3f7;break;
                case "Player":upper=0xff63e65e;lower=0xff21bb38;break;
                case "Studio":upper=0xffb17bff;lower=0xff7042d6;break;
                case "Radio":upper=0xffffa644;lower=0xfffa5e36;break;
                case "Weather":upper=0xff30bfff;lower=0xff087bd8;break;
            }
            p.setShader(new LinearGradient(0,0,320,192,upper,lower,Shader.TileMode.CLAMP));c.drawRect(0,0,320,192,p);p.setShader(null);
            c.translate(112,48);c.scale(3,3);p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);path.reset();
            switch(name){
                case "Music":
                    path.moveTo(12,7);path.lineTo(27,4);path.lineTo(27,24);path.lineTo(24,24);path.lineTo(24,10);path.lineTo(12,12);path.lineTo(12,27);path.lineTo(9,27);path.lineTo(9,8);path.close();c.drawPath(path,p);c.drawOval(3,23,12,29,p);c.drawOval(18,20,27,26,p);break;
                case "Photos":
                    int[] colors={0xffffaa17,0xffff7338,0xffef4b65,0xffc064bb,0xff6d82df,0xff59bce5,0xff63c991,0xffbad453};
                    for(int i=0;i<8;i++){c.save();c.rotate(i*45,16,16);p.setColor(colors[i]);p.setAlpha(205);c.drawOval(11,2,21,18,p);c.restore();}p.setAlpha(255);break;
                case "Files":
                    path.moveTo(2,8);path.quadTo(2,6,4,6);path.lineTo(13,6);path.lineTo(16,10);path.lineTo(28,10);path.quadTo(30,10,30,12);path.lineTo(30,25);path.quadTo(30,27,28,27);path.lineTo(4,27);path.quadTo(2,27,2,25);path.close();c.drawPath(path,p);break;
                case "Studio":
                    c.drawRoundRect(2,8,23,25,3,3,p);path.moveTo(24,12);path.lineTo(31,8);path.lineTo(31,25);path.lineTo(24,21);path.close();c.drawPath(path,p);break;
                case "Weather":
                    p.setColor(0xffffd354);c.drawCircle(11,11,7,p);p.setColor(Color.WHITE);c.drawRoundRect(5,17,30,26,4,4,p);c.drawCircle(17,18,6,p);c.drawCircle(24,20,5,p);break;
                case "Radio":
                    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setStrokeCap(Paint.Cap.ROUND);c.drawArc(2,2,30,30,135,90,false,p);c.drawArc(2,2,30,30,-45,90,false,p);c.drawArc(8,8,24,24,135,90,false,p);c.drawArc(8,8,24,24,-45,90,false,p);p.setStyle(Paint.Style.FILL);c.drawCircle(16,16,2.5f,p);c.drawRoundRect(14.8f,16,17.2f,29,1,1,p);break;
                default:
                    path.moveTo(10,5);path.lineTo(29,16);path.lineTo(10,27);path.close();c.drawPath(path,p);
                    if(name.equals("Cinema")){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f);c.drawRoundRect(1,2,31,30,5,5,p);}break;
            }
            p.setStyle(Paint.Style.FILL);c.restore();
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.OPAQUE;}
    }
}
