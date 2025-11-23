package com.llmhouse.edgetoedge;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Capacitor Edge-to-Edge Plugin
 * Provides native edge-to-edge display control for Android 11-16 (API 30-36)
 */
@CapacitorPlugin(name = "EdgeToEdge")
public class EdgeToEdgePlugin extends Plugin {

    private EdgeToEdge implementation;

    @Override
    public void load() {
        implementation = new EdgeToEdge(getActivity());
    }

    /**
     * Enable edge-to-edge mode
     */
    @PluginMethod
    public void enable(PluginCall call) {
        implementation.enable();
        call.resolve();
    }

    /**
     * Disable edge-to-edge mode
     */
    @PluginMethod
    public void disable(PluginCall call) {
        implementation.disable();
        call.resolve();
    }

    /**
     * Set system bars to transparent
     */
    @PluginMethod
    public void setTransparentSystemBars(PluginCall call) {
        Boolean statusBar = call.getBoolean("statusBar", true);
        Boolean navigationBar = call.getBoolean("navigationBar", true);
        
        implementation.setTransparentSystemBars(statusBar, navigationBar);
        call.resolve();
    }

    /**
     * Set system bar colors
     */
    @PluginMethod
    public void setSystemBarColors(PluginCall call) {
        String statusBarColor = call.getString("statusBarColor");
        String navigationBarColor = call.getString("navigationBarColor");
        
        implementation.setSystemBarColors(statusBarColor, navigationBarColor);
        call.resolve();
    }

    /**
     * Set system bar appearance (light/dark icons)
     */
    @PluginMethod
    public void setSystemBarAppearance(PluginCall call) {
        String statusBarStyle = call.getString("statusBarStyle");
        String navigationBarStyle = call.getString("navigationBarStyle");
        
        implementation.setSystemBarAppearance(statusBarStyle, navigationBarStyle);
        call.resolve();
    }

    /**
     * Get system bar insets
     */
    @PluginMethod
    public void getSystemBarInsets(PluginCall call) {
        JSObject result = implementation.getSystemBarInsets();
        call.resolve(result);
    }
}
