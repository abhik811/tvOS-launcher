package com.tvos.launcher;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Layered translucent material. Shaders change only with bounds or focus, with no live blur. */
final class GlassDrawable extends Drawable {
    private final Paint material = new Paint(3), edge = new Paint(3), shine = new Paint(3);
    private final RectF shape = new RectF();
    private final float corner, density;
    private final int strength;
    private int alpha = 255;
    GlassDrawable(float density, int strength, int radius) {
        this.density = density; this.strength = strength; corner = radius * density;
        edge.setStyle(Paint.Style.STROKE); edge.setStrokeWidth(density * .7f);
    }
    @Override public boolean isStateful() { return true; }
    private boolean selected() {
        for (int state : getState()) if (state == android.R.attr.state_focused || state == android.R.attr.state_pressed) return true;
        return false;
    }
    @Override protected boolean onStateChange(int[] states) { shaders(); invalidateSelf(); return true; }
    @Override protected void onBoundsChange(Rect bounds) { shaders(); }
    private void shaders() {
        Rect b = getBounds(); boolean focus = selected();
        int upper = focus ? 0xfaf6fcff : Color.argb(48 + strength * 21, 197, 225, 240);
        int lower = focus ? 0xf2e0edf4 : Color.argb(25 + strength * 14, 34, 65, 93);
        material.setShader(new LinearGradient(0, b.top, b.width(), b.bottom, upper, lower, Shader.TileMode.CLAMP));
        edge.setColor(focus ? 0xffffffff : 0x70e3f5ff);
        shine.setShader(new LinearGradient(0, b.top, 0, b.top + Math.max(1, b.height() * .42f),
                focus ? 0x18ffffff : 0x32ffffff, 0x00ffffff, Shader.TileMode.CLAMP));
    }
    @Override public void draw(Canvas c) {
        shape.set(getBounds()); shape.inset(density, density);
        material.setAlpha(alpha); edge.setAlpha(alpha); shine.setAlpha(alpha);
        c.drawRoundRect(shape, corner, corner, material);
        c.drawRoundRect(shape, corner, corner, shine);
        c.drawRoundRect(shape, corner, corner, edge);
    }
    @Override public void setAlpha(int value) { alpha = value; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { material.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
