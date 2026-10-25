package com.tvos.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;

/** Wide TV icon surface; artwork and focus ring share the same rounded mask. */
final class AppIconView extends FrameLayout {
    private final Path mask=new Path();
    AppIconView(Context context, Bitmap bitmap){
        super(context);setDuplicateParentStateEnabled(true);setClipToOutline(true);
        final float radius=Ui.dp(context,10);
        setOutlineProvider(new ViewOutlineProvider(){@Override public void getOutline(View view,Outline outline){outline.setRoundRect(0,0,view.getWidth(),view.getHeight(),radius);}});
        ImageView artwork=new ImageView(context);artwork.setImageBitmap(bitmap);artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);addView(artwork,new LayoutParams(-1,-1));
        StateListDrawable edges=new StateListDrawable();
        edges.addState(new int[]{android.R.attr.state_focused},edge(context,radius,0xf5ffffff,1.5f));
        edges.addState(new int[]{android.R.attr.state_pressed},edge(context,radius,0xf5ffffff,1.5f));
        edges.addState(new int[]{},edge(context,radius,0x30ffffff,.5f));setForeground(edges);setElevation(Ui.dp(context,3));
    }
    private GradientDrawable edge(Context context,float radius,int color,float width){
        GradientDrawable shape=new GradientDrawable();shape.setColor(Color.TRANSPARENT);shape.setCornerRadius(radius);shape.setStroke(Math.max(1,Ui.dp(context,width)),color);return shape;
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        int width=MeasureSpec.getSize(widthSpec);super.onMeasure(widthSpec,MeasureSpec.makeMeasureSpec(Math.round(width*3f/5),MeasureSpec.EXACTLY));
    }
    @Override protected void onSizeChanged(int w,int h,int oldW,int oldH){
        super.onSizeChanged(w,h,oldW,oldH);mask.reset();float radius=Ui.dp(getContext(),10);mask.addRoundRect(0,0,w,h,radius,radius,Path.Direction.CW);
    }
    @Override protected void dispatchDraw(Canvas canvas){
        // Outline clipping is hardware-only; keep software/native-view previews equivalent.
        if(canvas.isHardwareAccelerated()){super.dispatchDraw(canvas);return;}
        int save=canvas.save();canvas.clipPath(mask);super.dispatchDraw(canvas);canvas.restoreToCount(save);
    }
}
