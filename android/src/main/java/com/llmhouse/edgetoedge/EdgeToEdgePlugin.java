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
        
        // Setup keyboard listener to send events to JavaScript
        implementation.setupKeyboardListener(new EdgeToEdge.KeyboardListener() {
            private int lastHeight = 0;
            private boolean lastVisible = false;
            
            @Override
            public void onKeyboardChanged(int height, boolean isVisible) {
                // Only send events when state actually changes
                boolean heightChanged = height != lastHeight;
                boolean visibilityChanged = isVisible != lastVisible;
                
                if (heightChanged || visibilityChanged) {
                    JSObject event = new JSObject();
                    event.put("height", height);
                    event.put("isVisible", isVisible);
                    
                    // Send appropriate events
                    if (isVisible && !lastVisible) {
                        // Keyboard showing
                        notifyListeners("keyboardWillShow", event);
                        notifyListeners("keyboardDidShow", event);
                    } else if (!isVisible && lastVisible) {
                        // Keyboard hiding
                        notifyListeners("keyboardWillHide", event);
                        notifyListeners("keyboardDidHide", event);
                    }
                    
                    lastHeight = height;
                    lastVisible = isVisible;
                }
            }
        });
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

    /**
     * Get keyboard information (height and visibility)
     */
    @PluginMethod
    public void getKeyboardInfo(PluginCall call) {
        JSObject result = implementation.getKeyboardInfo();
        call.resolve(result);
    }
}
