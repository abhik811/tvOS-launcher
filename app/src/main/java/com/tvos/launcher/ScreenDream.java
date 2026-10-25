package com.tvos.launcher;

import android.service.dreams.DreamService;

public final class ScreenDream extends DreamService {
    @Override public void onAttachedToWindow(){super.onAttachedToWindow();setFullscreen(true);setInteractive(false);setContentView(ClockActivity.clock(this));}
}
