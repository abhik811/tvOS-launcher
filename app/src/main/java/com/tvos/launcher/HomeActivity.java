package com.tvos.launcher;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class HomeActivity extends Activity {
    private Preferences prefs;
    private FrameLayout root;
    private LinearLayout grid;
    private TextView shelf;
    private View controls;
    private String focused;
    private long displayedRevision=-1;
    private boolean allApps, receiverOn;
    private List<AppCatalog.Entry> apps=new ArrayList<>();
    private final Handler timer=new Handler(Looper.getMainLooper());
    private final Runnable idle=()->startActivity(new Intent(this,ClockActivity.class));
    private final BroadcastReceiver packages=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){render();}};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);prefs=new Preferences(this);if(saved!=null){allApps=saved.getBoolean("all");focused=saved.getString("focus");}render();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("all",allApps);out.putString("focus",focused);}
    private void render(){
        Ui.edgeToEdge(this);displayedRevision=prefs.revision();apps=AppCatalog.load(this,allApps);
        root=new FrameLayout(this);root.addView(new BackdropView(this),new FrameLayout.LayoutParams(-1,-1));
        shelf=Ui.text(this,allApps?"All Apps":"",32,Ui.WHITE);
        FrameLayout.LayoutParams title=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.START);title.leftMargin=Ui.dp(this,66);title.topMargin=(int)(getResources().getDisplayMetrics().heightPixels*.49f);root.addView(shelf,title);
        ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);
        int height=getResources().getDisplayMetrics().heightPixels;
        scroll.setPadding(Ui.dp(this,40),(int)(height*.67f),Ui.dp(this,40),Ui.dp(this,42));
        grid=new LinearLayout(this);grid.setId(R.id.home_apps);grid.setOrientation(LinearLayout.VERTICAL);grid.setClipChildren(false);scroll.addView(grid);root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        if(apps.isEmpty()){
            Ui.note(grid,allApps?"No launchable apps were found on this device.":"No TV apps found. Open All Apps or Settings.");
        }
        int columns=prefs.columns();
        int tileWidth=Math.max(80,(getResources().getDisplayMetrics().widthPixels-Ui.dp(this,108))/columns-Ui.dp(this,16));
        for(int start=0;start<apps.size();start+=columns){
            LinearLayout band=new LinearLayout(this);band.setClipChildren(false);band.setClipToPadding(false);band.setGravity(Gravity.CENTER_VERTICAL);band.setPadding(Ui.dp(this,9),Ui.dp(this,12),Ui.dp(this,9),Ui.dp(this,12));
            if(start==0)band.setBackground(new GlassDrawable(getResources().getDisplayMetrics().density,prefs.glass(),26));
            for(int col=0;col<columns;col++){
                int pos=start+col;
                if(pos>=apps.size()){band.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));continue;}
                AppCatalog.Entry app=apps.get(pos);View tile=tile(app,pos,tileWidth);
                LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,-2,1);cell.setMargins(Ui.dp(this,7),0,Ui.dp(this,7),0);band.addView(tile,cell);
            }
            LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-1,-2);spacing.bottomMargin=Ui.dp(this,24);grid.addView(band,spacing);
        }
        View library=Ui.button(this,allApps?"TV Apps":"All Apps",null,"apps",true,()->{allApps=!allApps;focused=null;render();});library.setTag("library");Ui.append(grid,library);
        LinearLayout status=new LinearLayout(this);status.setGravity(Gravity.CENTER);status.setPadding(Ui.dp(this,14),Ui.dp(this,6),Ui.dp(this,14),Ui.dp(this,6));
        status.setId(R.id.home_controls);status.setTag("controls");status.setFocusable(true);status.setFocusableInTouchMode(true);status.setClickable(true);status.setContentDescription("Control Center. Hold to open Settings.");
        status.setBackground(new GlassDrawable(getResources().getDisplayMetrics().density,prefs.glass(),28));
        TextClock clock=new TextClock(this);clock.setFormat12Hour("h:mm");clock.setFormat24Hour("HH:mm");clock.setTextSize(18);clock.setTextColor(Ui.textColors());clock.setDuplicateParentStateEnabled(true);clock.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);status.addView(clock);
        SymbolView gear=new SymbolView(this,"controls");gear.setDuplicateParentStateEnabled(true);LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(Ui.dp(this,23),Ui.dp(this,23));gp.setMarginStart(Ui.dp(this,15));status.addView(gear,gp);
        status.setOnClickListener(v->startActivity(new Intent(this,ControlCenterActivity.class)));
        status.setOnLongClickListener(v->{startActivity(new Intent(this,SettingsActivity.class));return true;});
        status.setOnFocusChangeListener((v,has)->{if(has)focused="controls";});
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-2,Ui.dp(this,44),Gravity.TOP|Gravity.END);sp.topMargin=Ui.dp(this,24);sp.rightMargin=Ui.dp(this,52);root.addView(status,sp);controls=status;
        status.setOnKeyListener((v,key,event)->{
            if(event.getAction()!=KeyEvent.ACTION_DOWN)return false;
            if(key==KeyEvent.KEYCODE_DPAD_DOWN){View target=grid.findViewWithTag(apps.isEmpty()?"library":apps.get(0).id);Ui.focus(target);return true;}
            return key==KeyEvent.KEYCODE_DPAD_UP;
        });
        setContentView(root);
        View target=focused==null?null:root.findViewWithTag(focused);if(target==null)target=apps.isEmpty()?controls:root.findViewWithTag(apps.get(0).id);Ui.focus(target);
    }
    private View tile(AppCatalog.Entry app,int position,int width){
        LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setClipChildren(false);tile.setTag(app.id);tile.setFocusable(true);tile.setFocusableInTouchMode(true);tile.setClickable(true);tile.setContentDescription(app.label);
        AppIconView banner=new AppIconView(this,AppCatalog.artwork(this,app,Math.min(width,512),Math.min(width,512)*3/5));
        tile.addView(banner,new LinearLayout.LayoutParams(-1,-2));
        TextView label=Ui.text(this,app.label,12,Ui.WHITE);label.setTextColor(Ui.WHITE);label.setVisibility(View.INVISIBLE);label.setShadowLayer(Ui.dp(this,2),0,Ui.dp(this,1),0xcc000000);label.setGravity(Gravity.CENTER);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);label.setPadding(Ui.dp(this,4),Ui.dp(this,5),Ui.dp(this,4),Ui.dp(this,5));label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);tile.addView(label);
        tile.setOnClickListener(v->Ui.open(this,app.launch));tile.setOnLongClickListener(v->{edit(app,position);return true;});
        tile.setOnFocusChangeListener((v,has)->{
            label.setVisibility(has?View.VISIBLE:View.INVISIBLE);
            if(has){focused=app.id;shelf.setText(app.label);banner.setElevation(Ui.dp(this,9));}else banner.setElevation(Ui.dp(this,3));
            v.animate().cancel();float scale=has?1.065f:1;
            if(Ui.animate(this))v.animate().scaleX(scale).scaleY(scale).translationY(has?-Ui.dp(this,3):0).setDuration(125).start();
            else{v.setScaleX(scale);v.setScaleY(scale);v.setTranslationY(0);}
        });
        tile.setOnKeyListener((v,key,event)->{if(position<prefs.columns()&&event.getAction()==KeyEvent.ACTION_DOWN&&key==KeyEvent.KEYCODE_DPAD_UP){Ui.focus(controls);return true;}return false;});
        return tile;
    }
    private void edit(AppCatalog.Entry app,int position){
        new AlertDialog.Builder(this).setTitle(app.label).setItems(new String[]{"Move earlier","Move later","Move to dock","App information"},(dialog,which)->{
            if(which==3){startActivity(DeviceActivity.intent(this,"app",app.id));return;}
            List<String> ids=new ArrayList<>();for(AppCatalog.Entry e:apps)ids.add(e.id);
            int destination=which==0?position-1:which==1?position+1:0;prefs.order(AppOrder.move(ids,app.id,destination));render();
        }).setNegativeButton(android.R.string.cancel,null).show();
    }
    @Override protected void onResume(){super.onResume();if(displayedRevision!=prefs.revision())render();
        if(!receiverOn){IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_PACKAGE_ADDED);f.addAction(Intent.ACTION_PACKAGE_REMOVED);f.addDataScheme("package");if(Build.VERSION.SDK_INT>=33)registerReceiver(packages,f,Context.RECEIVER_EXPORTED);else registerReceiver(packages,f);receiverOn=true;}
        restartIdle();autoLaunch();
    }
    private void autoLaunch(){
        String target=prefs.data.getString("startup","");if(target.isEmpty())return;
        int boot=Settings.Global.getInt(getContentResolver(),Settings.Global.BOOT_COUNT,-1);if(boot<0||prefs.data.getInt("startup.boot",-1)==boot)return;
        prefs.data.edit().putInt("startup.boot",boot).apply();Intent i=getPackageManager().getLeanbackLaunchIntentForPackage(target);if(i!=null)Ui.open(this,i);
    }
    private void restartIdle(){timer.removeCallbacks(idle);if(prefs.idleMinutes()>0)timer.postDelayed(idle,prefs.idleMinutes()*60_000L);}
    @Override public void onUserInteraction(){super.onUserInteraction();restartIdle();}
    @Override protected void onPause(){timer.removeCallbacks(idle);if(receiverOn){unregisterReceiver(packages);receiverOn=false;}super.onPause();}
    @Override public void onBackPressed(){if(allApps){allApps=false;focused=null;render();}else{scrollHome();}}
    private void scrollHome(){View v=apps.isEmpty()?controls:root.findViewWithTag(apps.get(0).id);Ui.focus(v);}
}
