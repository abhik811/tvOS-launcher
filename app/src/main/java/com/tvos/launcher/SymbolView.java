package com.tvos.launcher;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original vector symbols with consistent optical weight and cached geometry. */
final class SymbolView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tile = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path filled = new Path(), lines = new Path();
    private final boolean badge;
    private final boolean mechanical;
    private final Shader metal=new LinearGradient(0,0,32,32,0xff606975,0xff323a45,Shader.TileMode.CLAMP);
    SymbolView(Context context, String kind) { this(context,kind,false); }
    SymbolView(Context context, String kind, boolean badge) {
        super(context);this.badge=badge;mechanical=badge&&"settings".equals(kind);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeWidth(2.1f);
        tile.setShader(new LinearGradient(0,0,32,32,mechanical?new int[]{0xffd4d7dc,0xffa0a7b1,0xff737d8a}:new int[]{0xff9da5af,0xff59616d,0xff303742},null,Shader.TileMode.CLAMP));
        rim.setStyle(Paint.Style.STROKE);rim.setStrokeWidth(.35f);rim.setColor(0x90ffffff);
        geometry(kind);
    }
    private void line(float x,float y,float ex,float ey){lines.moveTo(x,y);lines.lineTo(ex,ey);}
    private void round(float l,float t,float r,float b,float radius){lines.addRoundRect(l,t,r,b,radius,radius,Path.Direction.CW);}
    private void geometry(String kind){
        switch(kind){
            case "wifi":
                lines.moveTo(3,11);lines.cubicTo(10,4,22,4,29,11);
                lines.moveTo(7.3f,16);lines.cubicTo(12,11.3f,20,11.3f,24.7f,16);
                lines.moveTo(11.7f,21);lines.quadTo(16,17,20.3f,21);
                filled.addCircle(16,26,1.8f,Path.Direction.CW);break;
            case "bluetooth":
                lines.moveTo(9,9);lines.lineTo(23,23);lines.lineTo(16,29);lines.lineTo(16,3);lines.lineTo(23,9);lines.lineTo(9,23);break;
            case "audio":
                filled.moveTo(3,12);filled.lineTo(9,12);filled.lineTo(16,6);filled.quadTo(17,5,17,7);filled.lineTo(17,25);filled.quadTo(17,27,16,26);filled.lineTo(9,20);filled.lineTo(3,20);filled.close();
                lines.moveTo(21,11);lines.cubicTo(24,14,24,18,21,21);
                lines.moveTo(25,7);lines.cubicTo(30,12,30,20,25,25);break;
            case "apps":
                for(int y=5;y<=18;y+=13)for(int x=5;x<=18;x+=13)filled.addRoundRect(x,y,x+9,y+9,2,2,Path.Direction.CW);break;
            case "home":
                filled.moveTo(2,15);filled.lineTo(14.5f,4);filled.quadTo(16,2.8f,17.5f,4);filled.lineTo(30,15);filled.lineTo(27,18);filled.lineTo(25,16);filled.lineTo(25,28);filled.lineTo(19,28);filled.lineTo(19,20);filled.lineTo(13,20);filled.lineTo(13,28);filled.lineTo(7,28);filled.lineTo(7,16);filled.lineTo(5,18);filled.close();break;
            case "display":
                round(2.5f,5,29.5f,23,2);line(16,23,16,28);line(10,28,22,28);break;
            case "saver":
                round(3,5,29,26,3);lines.moveTo(4,23);lines.lineTo(12,14);lines.lineTo(18,20);lines.lineTo(22,16);lines.lineTo(28,23);filled.addCircle(22,11,2.2f,Path.Direction.CW);break;
            case "remote":
                round(10,2,22,30,4);lines.addCircle(16,9,3.4f,Path.Direction.CW);filled.addCircle(13.5f,17,1,Path.Direction.CW);filled.addCircle(18.5f,17,1,Path.Direction.CW);filled.addCircle(13.5f,22,1,Path.Direction.CW);filled.addRoundRect(17.5f,20,19.5f,26,1,1,Path.Direction.CW);break;
            case "controls":
                line(3,10,29,10);line(3,22,29,22);
                filled.addRoundRect(8,6,13,14,2,2,Path.Direction.CW);filled.addRoundRect(20,18,25,26,2,2,Path.Direction.CW);break;
            case "storage":
                round(4,5,28,27,4);line(4,18,28,18);filled.addCircle(23,22.5f,1.2f,Path.Direction.CW);line(8,22.5f,17,22.5f);break;
            case "access":
                lines.addCircle(16,16,13,Path.Direction.CW);filled.addCircle(16,8,2,Path.Direction.CW);line(8.5f,13,23.5f,13);line(16,13,16,20);line(16,20,12,26);line(16,20,20,26);break;
            case "about":
                lines.addCircle(16,16,12,Path.Direction.CW);filled.addCircle(16,9,1.5f,Path.Direction.CW);line(16,14,16,24);break;
            default: gear();
        }
    }
    private void gear(){
        filled.setFillType(Path.FillType.EVEN_ODD);
        for(int tooth=0;tooth<8;tooth++)for(int corner=0;corner<4;corner++){
            double angle=Math.toRadians(tooth*45+new int[]{-22,-12,12,22}[corner]-90);
            float radius=(corner==1||corner==2)?13.5f:10.5f;
            float x=16+(float)Math.cos(angle)*radius,y=16+(float)Math.sin(angle)*radius;
            if(tooth==0&&corner==0)filled.moveTo(x,y);else filled.lineTo(x,y);
        }
        filled.close();filled.addCircle(16,16,mechanical?7.9f:5.1f,Path.Direction.CW);
        if(mechanical){filled.addCircle(16,16,6.6f,Path.Direction.CW);filled.addCircle(16,16,3.4f,Path.Direction.CW);}
    }
    @Override protected void drawableStateChanged(){super.drawableStateChanged();invalidate();}
    @Override protected void onDraw(Canvas canvas){
        canvas.save();canvas.scale(getWidth()/32f,getHeight()/32f);
        if(badge){canvas.drawRoundRect(.5f,.5f,31.5f,31.5f,7,7,tile);canvas.drawRoundRect(.6f,.6f,31.4f,31.4f,7,7,rim);canvas.translate(5,5);canvas.scale(22/32f,22/32f);}
        boolean focused=false;for(int state:getDrawableState())if(state==android.R.attr.state_focused||state==android.R.attr.state_pressed)focused=true;
        paint.setColor(focused&&!badge?Ui.INK:0xfff8fbff);
        paint.setStyle(Paint.Style.FILL);if(mechanical)paint.setShader(metal);canvas.drawPath(filled,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);canvas.drawPath(lines,paint);canvas.restore();
    }
}
