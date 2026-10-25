package com.tvos.launcher;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class RulesTest {
    @Test public void unavailableAndDuplicateAppsCannotCreateDockGaps(){assertEquals(Arrays.asList("b","a","c"),AppOrder.arrange(Arrays.asList("a","b","c","c"),Arrays.asList("missing","b","b")));}
    @Test public void reorderPreservesEveryOtherApp(){assertEquals(Arrays.asList("a","c","b"),AppOrder.move(Arrays.asList("a","b","c"),"b",2));}
    @Test public void movesStayWithinBounds(){assertEquals(Arrays.asList("c","a","b"),AppOrder.move(Arrays.asList("a","b","c"),"c",-5));assertEquals(Arrays.asList("b","c","a"),AppOrder.move(Arrays.asList("a","b","c"),"a",900));}
    @Test public void movingAnAbsentAppDoesNotCreateIt(){assertEquals(Arrays.asList("a","b"),AppOrder.move(Arrays.asList("a","b"),"missing",0));}
    @Test public void wifiControlsRespectTheAndroidTenBoundary(){assertTrue(DeviceRules.wifiControl(26));assertTrue(DeviceRules.wifiControl(28));assertFalse(DeviceRules.wifiControl(29));assertFalse(DeviceRules.wifiControl(36));}
    @Test public void bluetoothControlsRespectTheAndroidThirteenBoundary(){assertTrue(DeviceRules.bluetoothControl(32));assertFalse(DeviceRules.bluetoothControl(33));assertFalse(DeviceRules.bluetoothControl(36));}
    @Test public void encryptionCannotAccidentallyBecomeOpen(){for(String value:new String[]{"[WEP]","[WPA2-EAP]","[RSN-SAE]","[OWE]","[FUTURE][ESS]","[WAPI]"})assertEquals(DeviceRules.Security.SYSTEM_ONLY,DeviceRules.security(value));assertEquals(DeviceRules.Security.SYSTEM_ONLY,DeviceRules.security(null));}
    @Test public void personalAndTransitionNetworksUsePsk(){assertEquals(DeviceRules.Security.PERSONAL,DeviceRules.security("[RSN-PSK+SAE-CCMP][ESS]"));assertEquals(DeviceRules.Security.OPEN,DeviceRules.security("[ESS][WPS]"));}
    @Test public void quotedCredentialsRemainLiteral(){assertEquals("\"TV \\\"room\\\"\\\\\"",DeviceRules.quoted("TV \"room\"\\"));}
    @Test public void aRequestIsNotASuccess(){ConnectionAttempt a=new ConnectionAttempt();a.begin("room",100);assertEquals(ConnectionAttempt.State.REQUESTED,a.observe("room",false,false,200));}
    @Test public void anotherNetworkCannotConfirmTheTarget(){ConnectionAttempt a=new ConnectionAttempt();a.begin("new",0);assertEquals(ConnectionAttempt.State.REQUESTED,a.observe("old",true,true,100));}
    @Test public void onlyObservedAddressAndAssociationConfirmConnection(){ConnectionAttempt a=new ConnectionAttempt();a.begin("new",0);assertEquals(ConnectionAttempt.State.REQUESTED,a.observe("new",true,false,100));assertEquals(ConnectionAttempt.State.CONNECTED,a.observe("new",true,true,200));}
    @Test public void aTimeoutIsUnconfirmedRatherThanConnected(){ConnectionAttempt a=new ConnectionAttempt();a.begin("room",100);assertEquals(ConnectionAttempt.State.UNCONFIRMED,a.observe("room",false,true,45100));}
    @Test public void aLateSuccessfulObservationCanResolveATimeout(){ConnectionAttempt a=new ConnectionAttempt();a.begin("room",0);a.observe("",false,false,46000);assertEquals(ConnectionAttempt.State.CONNECTED,a.observe("room",true,true,47000));}
    @Test public void authenticationFailureDoesNotInventAConnection(){ConnectionAttempt a=new ConnectionAttempt();a.begin("room",0);a.authenticationFailed();assertEquals(ConnectionAttempt.State.UNCONFIRMED,a.state());}
}
