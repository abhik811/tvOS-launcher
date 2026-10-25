package com.tvos.launcher;

import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.view.View;
import java.io.InputStream;
import java.util.Random;

/** Original procedural landscape, rasterized once per layout and preference change. */
final class BackdropView extends View {
    private Bitmap texture;
    private final Paint sampling = new Paint(Paint.FILTER_BITMAP_FLAG);
    private String selection, image;
    BackdropView(Context c) { super(c); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
    void refresh() { selection = null; invalidate(); }
    @Override protected void onSizeChanged(int w,int h,int ow,int oh){ selection=null; }
    @Override protected void onDraw(Canvas canvas) {
        Preferences prefs = new Preferences(getContext());
        if (!prefs.backdrop().equals(selection) || !prefs.image().equals(image) || texture == null) {
            selection = prefs.backdrop(); image = prefs.image();
            texture = build(Math.max(1, Math.min(getWidth(), 960)), Math.max(1, Math.min(getHeight(), 540)));
        }
        canvas.drawBitmap(texture, null, new Rect(0,0,getWidth(),getHeight()), sampling);
    }
    private Bitmap build(int width,int height) {
        if ("photo".equals(selection) && !image.isEmpty()) {
            try {
                BitmapFactory.Options size = new BitmapFactory.Options(); size.inJustDecodeBounds=true;
                try(InputStream in=getContext().getContentResolver().openInputStream(Uri.parse(image))){BitmapFactory.decodeStream(in,null,size);}
                size.inJustDecodeBounds=false; size.inSampleSize=1; size.inPreferredConfig=Bitmap.Config.RGB_565;
                while(size.outWidth/size.inSampleSize>width*2 || size.outHeight/size.inSampleSize>height*2)size.inSampleSize*=2;
                try(InputStream in=getContext().getContentResolver().openInputStream(Uri.parse(image))){
                    Bitmap result=BitmapFactory.decodeStream(in,null,size); if(result!=null)return result;
                }
            }catch(Exception ignored){ /* A revoked or removed image falls back to the built-in scenery. */ }
        }
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
        Canvas c=new Canvas(bitmap);c.scale(width/960f,height/540f);
        if("black".equals(selection)){c.drawColor(Color.BLACK);return bitmap;}
        boolean dawn="dawn".equals(selection),night="night".equals(selection);
        int top=dawn?0xff48354e:night?0xff07122a:0xff152c48;
        int middle=dawn?0xffbb8178:night?0xff293e67:0xff4b7791;
        Paint p=new Paint(3);
        p.setShader(new LinearGradient(0,0,450,540,new int[]{top,middle,0xff081726},null,Shader.TileMode.CLAMP));c.drawRect(0,0,960,540,p);
        p.setShader(new RadialGradient(720,130,350,dawn?0x80ffe3c0:0x8066e2d9,0x005b89a3,Shader.TileMode.CLAMP));c.drawRect(0,0,960,540,p);
        p.setShader(null);
        if(night){Random r=new Random(8401);p.setColor(0x99d5e8ff);for(int k=0;k<36;k++)c.drawCircle(r.nextInt(950),r.nextInt(210),k%5==0?1.1f:.6f,p);}
        // Each ridge has a distinct, deliberately authored curved silhouette.
        Path ridge=new Path();ridge.moveTo(-50,360);ridge.cubicTo(75,295,165,220,260,160);ridge.cubicTo(320,190,366,270,410,285);ridge.cubicTo(500,245,565,155,630,120);ridge.cubicTo(700,160,780,275,1010,325);ridge.lineTo(1010,540);ridge.lineTo(-50,540);ridge.close();
        p.setColor(dawn?0xff66617b:night?0xff263753:0xff456d84);c.drawPath(ridge,p);
        ridge.reset();ridge.moveTo(-10,375);ridge.cubicTo(130,310,170,304,215,266);ridge.cubicTo(330,340,440,352,512,286);ridge.cubicTo(615,210,690,264,740,311);ridge.cubicTo(840,300,900,251,990,270);ridge.lineTo(990,550);ridge.lineTo(-10,550);ridge.close();
        p.setShader(new LinearGradient(0,220,0,540,dawn?0xff42445f:0xff203f5a,0xff102034,Shader.TileMode.CLAMP));c.drawPath(ridge,p);
        ridge.reset();ridge.moveTo(-20,450);ridge.cubicTo(130,361,235,425,380,440);ridge.cubicTo(510,455,555,365,690,383);ridge.cubicTo(805,397,883,442,980,378);ridge.lineTo(980,550);ridge.lineTo(-20,550);ridge.close();
        p.setShader(new LinearGradient(0,360,0,540,0xff172d43,0xff050d19,Shader.TileMode.CLAMP));c.drawPath(ridge,p);
        p.setShader(new LinearGradient(0,230,0,540,0x00000000,0x88030a15,Shader.TileMode.CLAMP));c.drawRect(0,0,960,540,p);
        return bitmap;
    }
}
