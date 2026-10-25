package com.tvos.launcher;

import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.text.format.Formatter;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Device controls use documented Android APIs and explicit handoffs for protected operations. */
public final class DeviceActivity extends Activity {
    private String kind,detail,focus,wifiPage="summary",selected;
    private Ui.Page page;
    private boolean listening,discovering,permissionScan;
    private final Map<String,BluetoothDevice> nearby=new TreeMap<>();
    private final Map<String,String> wifiFocus=new HashMap<>();
    private final ConnectionAttempt attempt=new ConnectionAttempt();
    private final Handler events=new Handler(Looper.getMainLooper());
    private final Runnable connectionCheck=()->{if(!isFinishing())draw();};
    static Intent intent(Context c,String kind,String detail){return new Intent(c,DeviceActivity.class).putExtra("panel",kind).putExtra("detail",detail);}
    private final BroadcastReceiver updates=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
        if(BluetoothDevice.ACTION_FOUND.equals(i.getAction())){BluetoothDevice d=i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);if(d!=null)nearby.put(d.getAddress(),d);}
        if(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(i.getAction())&&i.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,-1)==WifiManager.ERROR_AUTHENTICATING)attempt.authenticationFailed();
        draw();
    }};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);kind=getIntent().getStringExtra("panel");if(kind==null)kind="about";detail=getIntent().getStringExtra("detail");if(saved!=null){wifiPage=saved.getString("wifiPage","summary");selected=saved.getString("ssid");focus=saved.getString("focus");}}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("wifiPage",wifiPage);out.putString("ssid",selected);out.putString("focus",focus);}
    @Override protected void onResume(){super.onResume();
        if(!listening&&(kind.equals("network")||kind.equals("bluetooth"))){IntentFilter filter=new IntentFilter();
            if(kind.equals("network")){for(String a:new String[]{WifiManager.SCAN_RESULTS_AVAILABLE_ACTION,WifiManager.WIFI_STATE_CHANGED_ACTION,WifiManager.NETWORK_STATE_CHANGED_ACTION,WifiManager.SUPPLICANT_STATE_CHANGED_ACTION})filter.addAction(a);}
            else for(String a:new String[]{BluetoothAdapter.ACTION_STATE_CHANGED,BluetoothAdapter.ACTION_DISCOVERY_FINISHED,BluetoothDevice.ACTION_FOUND,BluetoothDevice.ACTION_BOND_STATE_CHANGED})filter.addAction(a);
            if(Build.VERSION.SDK_INT>=33)registerReceiver(updates,filter,Context.RECEIVER_EXPORTED);else registerReceiver(updates,filter);listening=true;
        }draw();
    }
    @Override protected void onPause(){events.removeCallbacks(connectionCheck);if(listening){unregisterReceiver(updates);listening=false;}try{BluetoothAdapter a=bluetooth();if(discovering&&a!=null)a.cancelDiscovery();}catch(SecurityException ignored){}discovering=false;super.onPause();}
    private void draw(){
        if(isFinishing())return;
        String title=switch(kind){case "network"->wifiPage.equals("list")?"Wi-Fi":wifiPage.equals("details")?selected:"Network";case "bluetooth"->"Bluetooth";case "audio"->"Audio";case "display"->"Display";case "storage"->"Storage";case "apps","app"->"Apps";case "about"->"About";default->label(kind);};
        String symbol=switch(kind){case "network"->"wifi";case "apps","app"->"apps";case "audio"->"audio";case "bluetooth"->"bluetooth";case "display"->"display";case "storage"->"storage";default->"about";};
        page=new Ui.Page(this,title==null?"Network":title,symbol,"Your device, in one place.");
        try{switch(kind){case "network":network();break;case "bluetooth":bluetoothPanel();break;case "audio":audio();break;case "display":display();break;case "storage":storage();break;case "apps":apps();break;case "app":appDetails();break;case "about":about();break;default:protectedPanel();}}
        catch(SecurityException denied){page.info("Access was denied. Review the permission in Android settings.");}
        catch(UnsupportedOperationException unavailable){page.info("This device does not support this control.");}
        if(!kind.equals("network")||wifiPage.equals("summary"))add("android","Open Android settings",null,()->Ui.system(this,systemAction()));
        add("back","Back",null,this::onBackPressed);page.restore(focus);
        if(attempt.state()==ConnectionAttempt.State.REQUESTED){events.removeCallbacks(connectionCheck);events.postDelayed(connectionCheck,45_000);}
    }
    private void add(String id,String title,String value,Runnable action){View row=page.row(id,title,value,true,()->{try{action.run();}catch(SecurityException e){toast("Android denied this action.");}catch(UnsupportedOperationException e){toast("This control is not supported here.");}});row.setOnFocusChangeListener((v,has)->{if(has)focus=id;});}
    private void toast(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
    private boolean location(){return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
    private void requestLocation(){permissionScan=true;requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},41);}
    private WifiManager wifi(){return getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)?(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE):null;}
    private String ssid(WifiInfo value){if(value==null||value.getSSID()==null||value.getSSID().contains("unknown ssid"))return "";String name=value.getSSID();return name.length()>1&&name.startsWith("\"")&&name.endsWith("\"")?name.substring(1,name.length()-1):name;}
    private boolean connected(WifiInfo current){return current!=null&&current.getSupplicantState()==SupplicantState.COMPLETED&&current.getIpAddress()!=0;}
    private void wifiNavigate(String next){wifiFocus.put(wifiPage,focus);wifiPage=next;focus=wifiFocus.get(next);draw();}
    private void network(){
        WifiManager radio=wifi();WifiInfo current=radio==null?null:radio.getConnectionInfo();
        attempt.observe(ssid(current),current!=null&&current.getIpAddress()!=0,current!=null&&current.getSupplicantState()==SupplicantState.COMPLETED,SystemClock.elapsedRealtime());
        ConnectivityManager cm=getSystemService(ConnectivityManager.class);NetworkCapabilities caps=cm==null?null:cm.getNetworkCapabilities(cm.getActiveNetwork());
        if(wifiPage.equals("summary")){
            String transport=caps==null?"Not connected":caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"Ethernet":caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"VPN":"Other";
            page.value("Connection",transport);page.value("Internet",caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"Available":"Not verified");
            if(radio!=null)add("wifi","Wi-Fi",ssid(current),()->wifiNavigate("list"));else page.info("This device has no Wi-Fi radio. Ethernet details are shown here.");
            LinkProperties links=cm==null?null:cm.getLinkProperties(cm.getActiveNetwork());if(links!=null)for(LinkAddress address:links.getLinkAddresses())page.value("IP address",address.getAddress().getHostAddress());return;
        }
        if(radio==null){page.info("Wi-Fi is unavailable on this device.");return;}
        if(wifiPage.equals("details")){
            boolean joined=connected(current)&&!ssid(current).isEmpty()&&ssid(current).equals(selected);
            String state=joined?"Connected":selected!=null&&selected.equals(attempt.target())&&attempt.state()==ConnectionAttempt.State.REQUESTED?"Connecting…":attempt.state()==ConnectionAttempt.State.UNCONFIRMED?"Connection not confirmed. Check the password and signal, then try again.":"Not connected";
            page.info(state);
            if(joined){page.value("Signal",(WifiManager.calculateSignalLevel(current.getRssi(),5)+1)+" of 5");int ip=current.getIpAddress();page.value("IP address",(ip&255)+"."+((ip>>>8)&255)+"."+((ip>>>16)&255)+"."+((ip>>>24)&255));}
            else if(attempt.state()!=ConnectionAttempt.State.REQUESTED)add("retry","Choose a network",null,()->wifiNavigate("list"));return;
        }
        if(DeviceRules.wifiControl(Build.VERSION.SDK_INT))add("power","Wi-Fi",radio.isWifiEnabled()?"On":"Off",()->{if(!radio.setWifiEnabled(!radio.isWifiEnabled()))toast("Android could not change Wi-Fi.");});
        else{page.info("Android manages connection changes on this device.");add("wifi.android","Open Android Wi-Fi",null,()->Ui.system(this,Settings.ACTION_WIFI_SETTINGS));}
        add("scan","Refresh networks",null,()->{if(!location()){requestLocation();return;}if(!radio.startScan())toast("Scan could not start. Check Wi-Fi and Location, then try again shortly.");});
        if(connected(current)&&!ssid(current).isEmpty())add("connected",ssid(current),"Connected",()->{selected=ssid(current);wifiNavigate("details");});
        if(!location()){page.info("Allow location permission to scan nearby networks. Android may also require Location to be on.");return;}
        List<ScanResult> found;
        try { found=new ArrayList<>(radio.getScanResults()); }
        catch(SecurityException denied){page.info("Location permission is required to show nearby networks.");return;}
        found.sort((a,b)->Integer.compare(b.level,a.level));Set<String> seen=new HashSet<>();
        if(found.isEmpty())page.info("No nearby networks found. Refresh to check again.");
        for(ScanResult result:found){if(result.SSID.isEmpty()||!seen.add(result.SSID)||result.SSID.equals(ssid(current)))continue;
            DeviceRules.Security security=DeviceRules.security(result.capabilities);
            add("ssid:"+result.SSID,result.SSID,security==DeviceRules.Security.OPEN?"Open":"Secured",()->{
                if(!DeviceRules.wifiControl(Build.VERSION.SDK_INT)){toast("Use Open Android Wi-Fi to connect.");return;}
                if(security==DeviceRules.Security.SYSTEM_ONLY){toast("This security type requires Android Wi-Fi settings.");return;}
                selected=result.SSID;
                if(security==DeviceRules.Security.OPEN)connect(result.SSID,null);
                else{EditText password=new EditText(this);password.setSingleLine(true);password.setHint("Password");password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    new AlertDialog.Builder(this).setTitle(result.SSID).setView(password).setPositiveButton("Connect",(d,w)->{try{connect(result.SSID,password.getText().toString());}catch(SecurityException denied){toast("Android denied this connection request.");}}).setNegativeButton(android.R.string.cancel,null).show();}
            });
        }
    }
    private void connect(String network,String password){
        if(!DeviceRules.wifiControl(Build.VERSION.SDK_INT))return;
        if(password!=null&&(password.length()<8||password.length()>63)){toast("Use a password with 8 to 63 characters.");return;}
        WifiManager manager=wifi();if(manager==null)return;
        WifiConfiguration config=new WifiConfiguration();config.SSID=DeviceRules.quoted(network);
        if(password==null)config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);else config.preSharedKey=DeviceRules.quoted(password);
        int id=manager.addNetwork(config);
        if(id<0||!manager.enableNetwork(id,true)||!manager.reconnect()){toast("Android could not start the connection.");return;}
        attempt.begin(network,SystemClock.elapsedRealtime());wifiNavigate("details");
    }
    private BluetoothAdapter bluetooth(){BluetoothManager manager=getSystemService(BluetoothManager.class);return manager==null?null:manager.getAdapter();}
    private boolean nearbyAllowed(){return Build.VERSION.SDK_INT<31||(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED);}
    private void bluetoothPanel(){
        try {
        BluetoothAdapter adapter=bluetooth();if(adapter==null){page.info("Bluetooth is unavailable on this device.");return;}
        if(!nearbyAllowed()){page.info("Allow Nearby devices to view and pair Bluetooth devices.");add("allow","Allow Nearby devices",null,()->requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN},42));return;}
        page.value("Bluetooth",adapter.isEnabled()?"On":"Off");
        if(!adapter.isEnabled()){add("enable","Turn Bluetooth on",null,()->{if(DeviceRules.bluetoothControl(Build.VERSION.SDK_INT)){if(!adapter.enable())toast("Android could not enable Bluetooth.");}else Ui.open(this,new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));});return;}
        page.info("Paired devices");Set<BluetoothDevice> paired=adapter.getBondedDevices();if(paired.isEmpty())page.info("No paired devices.");for(BluetoothDevice d:paired)page.value(deviceName(d),"Paired");
        add("discover",adapter.isDiscovering()?"Scanning…":"Discover nearby devices",null,()->{if(Build.VERSION.SDK_INT<31&&!location()){requestLocation();return;}startDiscovery();});
        for(BluetoothDevice d:nearby.values()){
            if(d.getBondState()==BluetoothDevice.BOND_BONDED)continue;
            add("bt:"+d.getAddress(),deviceName(d),d.getBondState()==BluetoothDevice.BOND_BONDING?"Pairing…":"Pair",()->{if(discovering)adapter.cancelDiscovery();discovering=false;if(!d.createBond())toast("Android could not start pairing.");});
        }
        if(DeviceRules.bluetoothControl(Build.VERSION.SDK_INT))add("disable","Turn Bluetooth off",null,()->new AlertDialog.Builder(this).setTitle("Turn Bluetooth off?").setMessage("Your remote may disconnect. Keep another input device available.").setPositiveButton("Turn off",(d,w)->{try{if(!adapter.disable())toast("Android denied this change.");}catch(SecurityException denied){toast("Android denied this change.");}}).setNegativeButton(android.R.string.cancel,null).show());
        else page.info("Use Android settings to turn Bluetooth off.");
        } catch(SecurityException denied){page.info("Nearby-device permission is required for Bluetooth.");}
    }
    private String deviceName(BluetoothDevice d){try{String name=d.getName();return name==null?"Bluetooth device":name;}catch(SecurityException denied){return "Bluetooth device";}}
    private void startDiscovery(){try{BluetoothAdapter a=bluetooth();if(a==null)return;if(discovering)a.cancelDiscovery();nearby.clear();discovering=a.startDiscovery();if(!discovering)toast("Discovery could not start. Check Bluetooth and required permissions.");}catch(SecurityException denied){toast("Nearby-device permission is required for discovery.");}}
    private void audio(){AudioManager manager=getSystemService(AudioManager.class);if(manager==null){page.info("Audio controls are unavailable.");return;}
        if(manager.isVolumeFixed())page.info("This is a fixed-volume output. Use your TV or receiver remote.");else{
            TextView value=Ui.text(this,"Media volume: "+manager.getStreamVolume(AudioManager.STREAM_MUSIC),18,Ui.WHITE);page.rows.addView(value);
            SeekBar slider=new SeekBar(this);slider.setTag("volume");slider.setContentDescription("Media volume");slider.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));slider.setProgress(manager.getStreamVolume(AudioManager.STREAM_MUSIC));slider.setFocusable(true);
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int n,boolean user){if(user)try{manager.setStreamVolume(AudioManager.STREAM_MUSIC,n,0);value.setText("Media volume: "+manager.getStreamVolume(AudioManager.STREAM_MUSIC));}catch(SecurityException e){toast("Android denied this volume change.");}}});
            page.rows.addView(slider,new android.widget.LinearLayout.LayoutParams(-1,Ui.dp(this,58)));
            add("mute",manager.isStreamMute(AudioManager.STREAM_MUSIC)?"Unmute":"Mute",null,()->{manager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_TOGGLE_MUTE,0);draw();});
        }
        page.info("Audio outputs");for(AudioDeviceInfo output:manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))page.info(output.getProductName().toString());page.info("Surround formats and output routing use Android settings.");
    }
    private void storage(){StatFs disk=new StatFs(Environment.getDataDirectory().getPath());page.value("Total",Formatter.formatFileSize(this,disk.getTotalBytes()));page.value("Available",Formatter.formatFileSize(this,disk.getAvailableBytes()));page.value("Used",Formatter.formatFileSize(this,disk.getTotalBytes()-disk.getAvailableBytes()));page.info("Android manages removable drives and other apps’ storage.");}
    private void display(){Display.Mode mode=getWindowManager().getDefaultDisplay().getMode();page.value("Resolution",mode.getPhysicalWidth()+" × "+mode.getPhysicalHeight());page.value("Refresh rate",String.format(Locale.getDefault(),"%.1f Hz",mode.getRefreshRate()));page.info("Resolution, HDR and HDMI controls use your device’s Android settings.");}
    private void apps(){for(AppCatalog.Entry app:AppCatalog.load(this,true))add(app.id,app.label,null,()->startActivity(intent(this,"app",app.id)));}
    private void appDetails(){if(detail==null)return;try{PackageInfo info=getPackageManager().getPackageInfo(detail,PackageManager.GET_PERMISSIONS);page.value("App",getPackageManager().getApplicationLabel(info.applicationInfo).toString());page.value("Version",info.versionName==null?"Unknown":info.versionName);page.info(detail);page.info("Granted permissions");
        if(info.requestedPermissions!=null)for(int i=0;i<info.requestedPermissions.length;i++)if(info.requestedPermissionsFlags!=null&&(info.requestedPermissionsFlags[i]&PackageInfo.REQUESTED_PERMISSION_GRANTED)!=0)try{page.info(getPackageManager().getPermissionInfo(info.requestedPermissions[i],0).loadLabel(getPackageManager()).toString());}catch(PackageManager.NameNotFoundException ignored){}
        add("manage","Manage app in Android settings",null,()->Ui.open(this,new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+detail))));
    }catch(PackageManager.NameNotFoundException e){page.info("This app is no longer installed.");}}
    private void about(){page.value("App","tvOS");page.value("Device",Build.MANUFACTURER+" "+Build.MODEL);page.value("Android",Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")");page.value("Security patch",Build.VERSION.SECURITY_PATCH);page.info("Firmware: "+Build.DISPLAY);try{page.value("Version",getPackageManager().getPackageInfo(getPackageName(),0).versionName);}catch(PackageManager.NameNotFoundException ignored){}add("license","License",null,()->license("LICENSE.txt"));}
    private void license(String name){try(java.io.BufferedReader reader=new java.io.BufferedReader(new java.io.InputStreamReader(getAssets().open("licenses/"+name),java.nio.charset.StandardCharsets.UTF_8))){new AlertDialog.Builder(this).setTitle("tvOS license").setMessage(reader.lines().collect(java.util.stream.Collectors.joining("\n"))).setPositiveButton(android.R.string.ok,null).show();}catch(java.io.IOException e){toast("License text is unavailable.");}}
    private String label(String id){return switch(id){case "language"->"Language";case "time"->"Date and Time";case "keyboard"->"Keyboard";case "access"->"Accessibility";case "dream"->"Screen Saver";case "accounts"->"Accounts";case "privacy"->"Privacy";case "updates"->"System Updates";case "developer"->"Developer Options";default->"Settings";};}
    private void protectedPanel(){if(kind.equals("language"))page.value("Language",Locale.getDefault().getDisplayName());if(kind.equals("time")){page.info(java.text.DateFormat.getDateTimeInstance().format(new Date()));page.info(TimeZone.getDefault().getID());}if(kind.equals("developer"))page.value("USB debugging",Settings.Global.getInt(getContentResolver(),Settings.Global.ADB_ENABLED,0)==1?"On":"Off");page.info("Android manages this setting. Use Open Android settings to change it.");}
    private String systemAction(){return switch(kind){case "network"->Settings.ACTION_WIFI_SETTINGS;case "bluetooth"->Settings.ACTION_BLUETOOTH_SETTINGS;case "audio"->Settings.ACTION_SOUND_SETTINGS;case "display"->Settings.ACTION_DISPLAY_SETTINGS;case "storage"->Settings.ACTION_INTERNAL_STORAGE_SETTINGS;case "apps","app"->Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS;case "language"->Settings.ACTION_LOCALE_SETTINGS;case "time"->Settings.ACTION_DATE_SETTINGS;case "keyboard"->Settings.ACTION_INPUT_METHOD_SETTINGS;case "access"->Settings.ACTION_ACCESSIBILITY_SETTINGS;case "dream"->Settings.ACTION_DREAM_SETTINGS;case "accounts"->Settings.ACTION_SYNC_SETTINGS;case "privacy"->Settings.ACTION_PRIVACY_SETTINGS;case "updates"->"android.settings.SYSTEM_UPDATE_SETTINGS";case "developer"->Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS;default->Settings.ACTION_SETTINGS;};}
    @Override public void onBackPressed(){if(kind.equals("network")&&!wifiPage.equals("summary")){wifiNavigate(wifiPage.equals("details")?"list":"summary");}else super.onBackPressed();}
    @Override public void onRequestPermissionsResult(int request,String[] names,int[] grants){super.onRequestPermissionsResult(request,names,grants);if(permissionScan&&location()){permissionScan=false;try{if(kind.equals("network")){WifiManager w=wifi();if(w!=null&&!w.startScan())toast("Scan could not start.");}else if(kind.equals("bluetooth"))startDiscovery();}catch(SecurityException denied){toast("Android denied discovery.");}}draw();}
}
