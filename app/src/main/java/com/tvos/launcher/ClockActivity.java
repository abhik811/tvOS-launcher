package com.tvos.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public final class ClockActivity extends Activity {
    @Override public void onCreate(Bundle state){super.onCreate(state);Ui.edgeToEdge(this);setContentView(clock(this));}
    static View clock(android.content.Context c){
        LinearLayout layout=new LinearLayout(c);layout.setOrientation(LinearLayout.VERTICAL);layout.setGravity(Gravity.CENTER);layout.setBackgroundColor(0xff070d18);
        TextClock hour=new TextClock(c);hour.setFormat12Hour("h:mm");hour.setFormat24Hour("HH:mm");hour.setTextSize(90);hour.setTextColor(Ui.WHITE);layout.addView(hour);
        TextClock day=new TextClock(c);day.setFormat12Hour("EEEE, MMMM d");day.setFormat24Hour("EEEE, MMMM d");day.setTextSize(18);day.setTextColor(Ui.MUTED);layout.addView(day);return layout;
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event){if(event.getAction()==KeyEvent.ACTION_UP)finish();return true;}
    @Override public boolean onTouchEvent(MotionEvent event){finish();return true;}
}
