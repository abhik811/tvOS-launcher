package com.tvos.launcher;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original geometric symbols, drawn at display size without bitmap assets. */
final class SymbolView extends View {
    private final String kind;
    private final Paint p = new Paint(3);
    SymbolView(Context c, String kind) { super(c); this.kind = kind; setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
    @Override protected void drawableStateChanged() { super.drawableStateChanged(); invalidate(); }
    @Override protected void onDraw(Canvas canvas) {
        canvas.save(); canvas.scale(getWidth() / 32f, getHeight() / 32f);
        boolean focused = false; for (int state : getDrawableState()) if (state == android.R.attr.state_focused) focused = true;
        p.setColor(focused ? 0xff142a39 : 0xffe5f4ff); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
        Path path = new Path();
        switch (kind) {
            case "wifi":
                canvas.drawArc(2, 8, 30, 34, 220, 100, false, p); canvas.drawArc(8, 15, 24, 31, 220, 100, false, p);
                p.setStyle(Paint.Style.FILL); canvas.drawCircle(16, 25, 1.6f, p); break;
            case "bluetooth":
                path.moveTo(10, 9); path.lineTo(22, 22); path.lineTo(16, 28); path.lineTo(16, 4); path.lineTo(22, 10); path.lineTo(10, 23); canvas.drawPath(path, p); break;
            case "audio":
                path.moveTo(4, 13); path.lineTo(10, 13); path.lineTo(16, 7); path.lineTo(16, 25); path.lineTo(10, 19); path.lineTo(4, 19); path.close(); canvas.drawPath(path,p);
                canvas.drawArc(15, 8, 28, 24, -65, 130, false,p); break;
            case "apps":
                for(int y=5;y<26;y+=9)for(int x=5;x<26;x+=9)canvas.drawRoundRect(x,y,x+5,y+5,1,1,p); break;
            case "home":
                path.moveTo(4,15);path.lineTo(16,5);path.lineTo(28,15);path.moveTo(7,13);path.lineTo(7,27);path.lineTo(13,27);path.lineTo(13,20);path.lineTo(19,20);path.lineTo(19,27);path.lineTo(25,27);path.lineTo(25,13);canvas.drawPath(path,p);break;
            case "display": case "saver":
                canvas.drawRoundRect(3,5,29,23,2,2,p);canvas.drawLine(16,23,16,28,p);canvas.drawLine(10,28,22,28,p);break;
            case "storage": canvas.drawRoundRect(5,5,27,27,3,3,p);canvas.drawLine(5,15,27,15,p);canvas.drawCircle(10,21,1,p);break;
            case "access":
                canvas.drawCircle(16,6,2.5f,p);canvas.drawLine(5,12,27,12,p);canvas.drawLine(16,12,16,20,p);canvas.drawLine(16,20,10,28,p);canvas.drawLine(16,20,22,28,p);break;
            case "about":canvas.drawCircle(16,16,12,p);canvas.drawPoint(16,9,p);canvas.drawLine(16,14,16,24,p);break;
            default:
                canvas.drawCircle(16,16,9,p);canvas.drawCircle(16,16,3,p);
                for(int i=0;i<8;i++){double a=i*Math.PI/4;canvas.drawLine(16+(float)Math.cos(a)*10,16+(float)Math.sin(a)*10,16+(float)Math.cos(a)*13,16+(float)Math.sin(a)*13,p);}
        }
        canvas.restore();
    }
}
