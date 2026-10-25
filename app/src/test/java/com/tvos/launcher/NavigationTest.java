package com.tvos.launcher;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(sdk={28,30,34},qualifiers="w960dp-h540dp-land-xhdpi")
public class NavigationTest {
    @Before public void clean(){new Preferences(RuntimeEnvironment.getApplication()).data.edit().clear().commit();}
    private View find(Activity a,String tag){return a.getWindow().getDecorView().findViewWithTag(tag);}
    @Test public void settingsBackReturnsThroughItsActualParent(){
        try(ActivityController<SettingsActivity> c=Robolectric.buildActivity(SettingsActivity.class).setup().visible()){
            SettingsActivity a=c.get();find(a,"general").performClick();find(a,"glass").performClick();assertEquals("Appearance",((android.widget.TextView)a.findViewById(R.id.page_title)).getText().toString());
            a.onBackPressed();assertEquals("General",((android.widget.TextView)a.findViewById(R.id.page_title)).getText().toString());a.onBackPressed();assertEquals("Settings",((android.widget.TextView)a.findViewById(R.id.page_title)).getText().toString());
        }
    }
    @Test public void launcherPreferencesAreRealAndPersisted(){try(ActivityController<SettingsActivity> c=Robolectric.buildActivity(SettingsActivity.class).setup().visible()){
        SettingsActivity a=c.get();find(a,"access").performClick();find(a,"motion").performClick();assertFalse(new Preferences(a).motion());assertNull(shadowOf(a).getNextStartedActivity());
    }}
    @Test public void networkEntryOpensOurOwnActivity(){try(ActivityController<SettingsActivity> c=Robolectric.buildActivity(SettingsActivity.class).setup().visible()){
        SettingsActivity a=c.get();find(a,"network").performClick();Intent next=shadowOf(a).getNextStartedActivity();assertEquals(DeviceActivity.class.getName(),next.getComponent().getClassName());assertEquals("network",next.getStringExtra("panel"));
    }}
    @Test public void missingWifiDoesNotOfferAnInvalidRadioControl(){shadowOf(RuntimeEnvironment.getApplication().getPackageManager()).setSystemFeature(PackageManager.FEATURE_WIFI,false);
        try(ActivityController<DeviceActivity> c=Robolectric.buildActivity(DeviceActivity.class,DeviceActivity.intent(RuntimeEnvironment.getApplication(),"network",null)).setup().visible()){
            assertNull(find(c.get(),"wifi"));assertNotNull(find(c.get(),"android"));assertNull(shadowOf(c.get()).getNextStartedActivity());
        }
    }
    @Test public void modernAndroidNeverOffersLegacyWifiToggle(){shadowOf(RuntimeEnvironment.getApplication().getPackageManager()).setSystemFeature(PackageManager.FEATURE_WIFI,true);
        try(ActivityController<DeviceActivity> c=Robolectric.buildActivity(DeviceActivity.class,DeviceActivity.intent(RuntimeEnvironment.getApplication(),"network",null)).setup().visible()){
            DeviceActivity a=c.get();find(a,"wifi").performClick();assertEquals(Build.VERSION.SDK_INT<=28,find(a,"power")!=null);assertEquals(Build.VERSION.SDK_INT>=29,find(a,"wifi.android")!=null);assertNotNull(find(a,"scan"));
            a.onBackPressed();assertNotNull(find(a,"wifi"));
        }
    }
    @Test public void controlCenterDoesNotRequireAccessibilityOrPrivilegedPermissions(){try(ActivityController<ControlCenterActivity> c=Robolectric.buildActivity(ControlCenterActivity.class).setup().visible()){
        find(c.get(),"settings").performClick();assertEquals(SettingsActivity.class.getName(),shadowOf(c.get()).getNextStartedActivity().getComponent().getClassName());
    }}
    @Test public void populatedHomeLaunchesExactlyTheChosenApp(){DemoApps.install(RuntimeEnvironment.getApplication());
        try(ActivityController<HomeActivity> c=Robolectric.buildActivity(HomeActivity.class).setup().visible()){
            HomeActivity a=c.get();View cinema=find(a,"example.fixture.cinema");assertNotNull(cinema);cinema.performClick();
            Intent launched=shadowOf(a).getNextStartedActivity();assertEquals("example.fixture.cinema",launched.getComponent().getPackageName());
        }
    }
    @Test public void emptyHomeStillHasReachableControls(){try(ActivityController<HomeActivity> c=Robolectric.buildActivity(HomeActivity.class).setup().visible()){
        assertTrue(c.get().findViewById(R.id.home_controls).isFocusable());assertNotNull(find(c.get(),"library"));
    }}
}
