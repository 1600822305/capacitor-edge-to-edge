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
        
        // Setup keyboard listener using official Capacitor Keyboard plugin approach
        implementation.setupKeyboardListener(new EdgeToEdge.KeyboardListener() {
            @Override
            public void onKeyboardWillShow(int keyboardHeight) {
                JSObject event = new JSObject();
                event.put("keyboardHeight", keyboardHeight);
                
                // Trigger window event (for compatibility with Capacitor Keyboard plugin)
                String data = "{ 'keyboardHeight': " + keyboardHeight + " }";
                getBridge().triggerWindowJSEvent("keyboardWillShow", data);
                
                // Notify listeners
                notifyListeners("keyboardWillShow", event);
            }
            
            @Override
            public void onKeyboardDidShow(int keyboardHeight) {
                JSObject event = new JSObject();
                event.put("keyboardHeight", keyboardHeight);
                
                // Trigger window event
                String data = "{ 'keyboardHeight': " + keyboardHeight + " }";
                getBridge().triggerWindowJSEvent("keyboardDidShow", data);
                
                // Notify listeners
                notifyListeners("keyboardDidShow", event);
            }
            
            @Override
            public void onKeyboardWillHide() {
                JSObject event = new JSObject();
                event.put("keyboardHeight", 0);
                
                // Trigger window event
                getBridge().triggerWindowJSEvent("keyboardWillHide");
                
                // Notify listeners
                notifyListeners("keyboardWillHide", event);
            }
            
            @Override
            public void onKeyboardDidHide() {
                JSObject event = new JSObject();
                event.put("keyboardHeight", 0);
                
                // Trigger window event
                getBridge().triggerWindowJSEvent("keyboardDidHide");
                
                // Notify listeners
                notifyListeners("keyboardDidHide", event);
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

    /**
     * Show the keyboard (Android only)
     * Based on official Capacitor Keyboard plugin
     */
    @PluginMethod
    public void show(PluginCall call) {
        implementation.showKeyboard();
        call.resolve();
    }

    /**
     * Hide the keyboard
     * Based on official Capacitor Keyboard plugin
     */
    @PluginMethod
    public void hide(PluginCall call) {
        boolean success = implementation.hideKeyboard();
        if (!success) {
            call.reject("Can't close keyboard, not currently focused");
        } else {
            call.resolve();
        }
    }
}
