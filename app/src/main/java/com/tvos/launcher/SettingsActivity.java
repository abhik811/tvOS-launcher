package com.tvos.launcher;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import java.util.*;

public final class SettingsActivity extends Activity {
    private String section="root",focused;
    private final Map<String,String> remembered=new HashMap<>();
    private final ArrayList<String> parents=new ArrayList<>();
    private Preferences prefs;
    private Ui.Page page;
    @Override public void onCreate(Bundle state){super.onCreate(state);prefs=new Preferences(this);if(state!=null){section=state.getString("section","root");focused=state.getString("focus");ArrayList<String> trail=state.getStringArrayList("parents");if(trail!=null)parents.addAll(trail);}draw();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("section",section);out.putString("focus",focused);out.putStringArrayList("parents",parents);}
    private void navigate(String target){remembered.put(section,focused);parents.add(section);section=target;focused=remembered.get(target);draw();}
    private void add(String id,String label,String value,Runnable action){View v=page.row(id,label,value,true,action);v.setOnFocusChangeListener((item,has)->{if(has)focused=id;});}
    private void device(String id,String label){add(id,label,null,()->startActivity(DeviceActivity.intent(this,id,null)));}
    private void draw(){
        String title=switch(section){case "home"->"Home Screen";case "glass"->"Appearance";case "video"->"Video and Audio";case "remotes"->"Remotes and Devices";case "access"->"Accessibility";case "saver"->"Screen Saver";case "general"->"General";case "system"->"System";default->"Settings";};
        String icon=switch(section){case "home","glass"->"home";case "video"->"display";case "access"->"access";case "remotes"->"bluetooth";case "saver"->"saver";case "general"->"about";default->"settings";};
        page=new Ui.Page(this,title,icon,"Personalize Home and manage your device.");
        switch(section){
            case "root":
                for(String[] item:new String[][]{{"general","General"},{"home","Home Screen"},{"video","Video and Audio"},{"saver","Screen Saver"},{"remotes","Remotes and Devices"},{"access","Accessibility"}}){String key=item[0];add(key,item[1],null,()->navigate(key));}
                device("apps","Apps");device("network","Network");add("system","System",null,()->navigate("system"));break;
            case "general":
                add("glass","Appearance",null,()->navigate("glass"));device("language","Language");device("time","Date and Time");device("storage","Storage");device("about","About");break;
            case "home":
                add("columns","Apps across dock",Integer.toString(prefs.columns()),()->pick("Dock size",new String[]{"4","5","6","7"},prefs.columns()-4,k->{prefs.number("columns",k+4);draw();}));
                add("background","Background",backgroundName(),this::chooseBackground);
                add("photo","Choose local photo",null,()->Ui.open(this,new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*").putExtra(Intent.EXTRA_LOCAL_ONLY,true)));
                // Use an activity result for persistent, user-selected document access.
                View photo=page.rows.findViewWithTag("photo");photo.setOnClickListener(v->{try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*").putExtra(Intent.EXTRA_LOCAL_ONLY,true),20);}catch(ActivityNotFoundException e){android.widget.Toast.makeText(this,"Install a file picker to choose a photo.",android.widget.Toast.LENGTH_LONG).show();}});
                add("startup","Startup app",startupName(),this::chooseStartup);
                add("default","Default Home app",null,()->Ui.system(this,Settings.ACTION_HOME_SETTINGS));
                page.info("Hold OK on any Home tile to move it or place it in the dock.");break;
            case "glass":
                add("glass","Glass finish",new String[]{"Subtle","Balanced","Bright"}[prefs.glass()-1],()->pick("Glass finish",new String[]{"Subtle","Balanced","Bright"},prefs.glass()-1,k->{prefs.number("glass",k+1);draw();}));
                add("motion","Animations",prefs.motion()?"On":"Off",()->{prefs.motion(!prefs.motion());draw();});
                add("background","Background",backgroundName(),this::chooseBackground);
                add("reset","Reset appearance",null,()->new AlertDialog.Builder(this).setTitle("Reset Home preferences?").setMessage("This also clears app order and your startup choice.").setPositiveButton("Reset",(d,w)->{prefs.reset();draw();}).setNegativeButton(android.R.string.cancel,null).show());break;
            case "video":device("display","Display");device("audio","Audio");break;
            case "remotes":device("bluetooth","Bluetooth");device("keyboard","Keyboard");break;
            case "access":add("motion","Reduce motion",prefs.motion()?"Off":"On",()->{prefs.motion(!prefs.motion());draw();});device("access","Android accessibility");break;
            case "saver":
                add("clock","Start Screen Saver",null,()->startActivity(new Intent(this,ClockActivity.class)));
                int[] values={0,5,10,15};int current=0;for(int n=0;n<values.length;n++)if(values[n]==prefs.idleMinutes())current=n;final int chosen=current;
                add("idle","Start after inactivity",prefs.idleMinutes()==0?"Off":prefs.idleMinutes()+" minutes",()->pick("Start after inactivity",new String[]{"Off","5 minutes","10 minutes","15 minutes"},chosen,k->{prefs.number("idle",values[k]);draw();}));device("dream","Android Screen Saver");break;
            case "system":device("accounts","Accounts");device("privacy","Privacy");device("updates","System Updates");device("developer","Developer Options");add("android","Open Android Settings",null,()->Ui.system(this,Settings.ACTION_SETTINGS));break;
        }
        page.restore(focused);
    }
    private String backgroundName(){return switch(prefs.backdrop()){case "dawn"->"Desert Dawn";case "night"->"Alpine Night";case "black"->"Solid black";case "photo"->"Local photo";default->"Aurora Coast";};}
    private void chooseBackground(){String[] modes={"aurora","dawn","night","black"};int index=Arrays.asList(modes).indexOf(prefs.backdrop());pick("Background",new String[]{"Aurora Coast","Desert Dawn","Alpine Night","Solid black"},Math.max(0,index),k->{prefs.text("backdrop",modes[k]);draw();});}
    private void pick(String title,String[] names,int current,java.util.function.IntConsumer action){new AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(names,current,(d,n)->{d.dismiss();action.accept(n);}).setNegativeButton(android.R.string.cancel,null).show();}
    private String startupName(){String pkg=prefs.data.getString("startup","");if(pkg.isEmpty())return "None";try{return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(android.content.pm.PackageManager.NameNotFoundException e){return "App unavailable";}}
    private void chooseStartup(){List<AppCatalog.Entry> apps=AppCatalog.load(this,false);String[] names=new String[apps.size()+1];names[0]="None";int selected=0;for(int i=0;i<apps.size();i++){names[i+1]=apps.get(i).label;if(apps.get(i).id.equals(prefs.data.getString("startup","")))selected=i+1;}pick("Startup app",names,selected,n->{prefs.text("startup",n==0?"":apps.get(n-1).id);prefs.data.edit().putInt("startup.boot",Settings.Global.getInt(getContentResolver(),Settings.Global.BOOT_COUNT,-1)).apply();draw();});}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==20&&result==RESULT_OK&&data!=null&&data.getData()!=null){Uri uri=data.getData();try{if((data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION)!=0)getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(SecurityException ignored){}prefs.text("image",uri.toString());prefs.text("backdrop","photo");draw();}}
    @Override public void onBackPressed(){if(!section.equals("root")){section=parents.isEmpty()?"root":parents.remove(parents.size()-1);focused=remembered.get(section);draw();}else super.onBackPressed();}
}
