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
        // Matches @capacitor/keyboard event structure
        implementation.setupKeyboardListener(new EdgeToEdge.KeyboardListener() {
            @Override
            public void onKeyboardWillShow(int height, boolean isVisible) {
                // Send Will events (animation start)
                JSObject event = new JSObject();
                event.put("keyboardHeight", height);  // Match @capacitor/keyboard field name
                
                if (isVisible) {
                    notifyListeners("keyboardWillShow", event);
                } else {
                    notifyListeners("keyboardWillHide", new JSObject());
                }
            }
            
            @Override
            public void onKeyboardDidShow(int height, boolean isVisible) {
                // Send Did events (animation end)
                JSObject event = new JSObject();
                event.put("keyboardHeight", height);  // Match @capacitor/keyboard field name
                
                if (isVisible) {
                    notifyListeners("keyboardDidShow", event);
                } else {
                    notifyListeners("keyboardDidHide", new JSObject());
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
