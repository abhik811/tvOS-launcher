package com.tvos.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public final class ControlCenterActivity extends Activity {
    @Override public void onCreate(Bundle state){
        super.onCreate(state);Ui.edgeToEdge(this);
        FrameLayout overlay=new FrameLayout(this);overlay.setBackgroundColor(0x18030a15);overlay.setOnClickListener(v->finish());
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(Ui.dp(this,13),Ui.dp(this,13),Ui.dp(this,13),Ui.dp(this,13));panel.setBackground(new GlassDrawable(getResources().getDisplayMetrics().density,new Preferences(this).glass(),30));panel.setOnClickListener(v->{});panel.setElevation(Ui.dp(this,12));
        FrameLayout.LayoutParams place=new FrameLayout.LayoutParams(Ui.dp(this,324),-2,Gravity.TOP|Gravity.END);place.topMargin=Ui.dp(this,25);place.setMarginEnd(Ui.dp(this,35));overlay.addView(panel,place);
        LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);heading.setPadding(Ui.dp(this,7),Ui.dp(this,4),Ui.dp(this,7),Ui.dp(this,16));TextView caption=Ui.text(this,"Control Center",15,Ui.WHITE);caption.setShadowLayer(Ui.dp(this,2),0,Ui.dp(this,1),0xff071323);heading.addView(caption,new LinearLayout.LayoutParams(0,-2,1));
        TextClock time=new TextClock(this);time.setTextSize(14);time.setTextColor(Ui.WHITE);time.setShadowLayer(Ui.dp(this,2),0,Ui.dp(this,1),0xff071323);time.setFormat12Hour("h:mm");time.setFormat24Hour("HH:mm");heading.addView(time);panel.addView(heading);
        LinearLayout feature=new LinearLayout(this);
        View saver=tile("Screen Saver","saver",()->launch(new Intent(this,ClockActivity.class)));LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,Ui.dp(this,144),1);left.setMarginEnd(Ui.dp(this,8));feature.addView(saver,left);
        LinearLayout pair=new LinearLayout(this);pair.setOrientation(LinearLayout.VERTICAL);
        pair.addView(tile("Network","wifi",()->launch(DeviceActivity.intent(this,"network",null))),new LinearLayout.LayoutParams(-1,Ui.dp(this,68)));
        LinearLayout.LayoutParams next=new LinearLayout.LayoutParams(-1,Ui.dp(this,68));next.topMargin=Ui.dp(this,8);pair.addView(tile("Audio","audio",()->launch(DeviceActivity.intent(this,"audio",null))),next);
        feature.addView(pair,new LinearLayout.LayoutParams(0,-2,1));panel.addView(feature);
        LinearLayout shortcuts=new LinearLayout(this);
        View settings=tile("Settings","settings",()->launch(new Intent(this,SettingsActivity.class)));settings.setTag("settings");shortcut(shortcuts,settings);
        shortcut(shortcuts,tile("Remotes","remote",()->launch(DeviceActivity.intent(this,"bluetooth",null))));shortcut(shortcuts,tile("Apps","apps",()->launch(DeviceActivity.intent(this,"apps",null))));
        LinearLayout.LayoutParams row=new LinearLayout.LayoutParams(-1,-2);row.topMargin=Ui.dp(this,9);panel.addView(shortcuts,row);
        setContentView(overlay);Ui.focus(saver);
    }
    private void shortcut(LinearLayout target,View child){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,70),1);p.setMargins(Ui.dp(this,2),0,Ui.dp(this,2),0);target.addView(child,p);}
    private View tile(String label,String symbol,Runnable callback){
        LinearLayout cell=new LinearLayout(this);cell.setOrientation(LinearLayout.VERTICAL);cell.setGravity(Gravity.CENTER);cell.setPadding(Ui.dp(this,5),Ui.dp(this,6),Ui.dp(this,5),Ui.dp(this,6));cell.setFocusable(true);cell.setFocusableInTouchMode(true);cell.setClickable(true);cell.setContentDescription(label);cell.setBackground(new GlassDrawable(getResources().getDisplayMetrics().density,new Preferences(this).glass(),19));
        SymbolView icon=new SymbolView(this,symbol);icon.setDuplicateParentStateEnabled(true);cell.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,25),Ui.dp(this,25)));
        TextView name=Ui.text(this,label,13,Ui.WHITE);name.setTextColor(Ui.textColors());name.setDuplicateParentStateEnabled(true);name.setGravity(Gravity.CENTER);name.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=Ui.dp(this,5);cell.addView(name,lp);cell.setOnClickListener(v->callback.run());return cell;
    }
    private void launch(Intent intent){Ui.open(this,intent);finish();}
    @Override protected void onPause(){super.onPause();finish();}
}
