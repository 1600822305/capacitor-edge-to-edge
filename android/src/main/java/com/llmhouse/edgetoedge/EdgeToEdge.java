package com.llmhouse.edgetoedge;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;

import java.util.List;

/**
 * EdgeToEdge implementation for Android 11-16 (API 30-36)
 * Provides native control over system bars (status bar and navigation bar)
 */
public class EdgeToEdge {

    private static final String TAG = "EdgeToEdge";
    private Activity activity;

    public EdgeToEdge(Activity activity) {
        this.activity = activity;
    }

    /**
     * Enable edge-to-edge mode (content draws behind system bars)
     * Works on Android 11-16 (API 30-36)
     */
    public void enable() {
        activity.runOnUiThread(() -> {
            Window window = activity.getWindow();
            
            // Use WindowCompat for backward compatibility
            WindowCompat.setDecorFitsSystemWindows(window, false);
            
            Logger.info(TAG, "Edge-to-edge mode enabled");
        });
    }

    /**
     * Disable edge-to-edge mode (content below system bars)
     */
    public void disable() {
        activity.runOnUiThread(() -> {
            Window window = activity.getWindow();
            
            // Re-enable insets fitting
            WindowCompat.setDecorFitsSystemWindows(window, true);
            
            Logger.info(TAG, "Edge-to-edge mode disabled");
        });
    }

    /**
     * Set system bars to transparent
     */
    public void setTransparentSystemBars(boolean statusBar, boolean navigationBar) {
        activity.runOnUiThread(() -> {
            Window window = activity.getWindow();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+)
                if (statusBar) {
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
                if (navigationBar) {
                    window.setNavigationBarColor(Color.TRANSPARENT);
                }
            }
            
            Logger.info(TAG, "System bars set to transparent");
        });
    }

    /**
     * Set system bar colors
     */
    public void setSystemBarColors(String statusBarColor, String navigationBarColor) {
        activity.runOnUiThread(() -> {
            Window window = activity.getWindow();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (statusBarColor != null) {
                    try {
                        int color = Color.parseColor(statusBarColor);
                        window.setStatusBarColor(color);
                        Logger.info(TAG, "Status bar color set to: " + statusBarColor);
                    } catch (IllegalArgumentException e) {
                        Logger.error(TAG, "Invalid status bar color: " + statusBarColor, e);
                    }
                }
                
                if (navigationBarColor != null) {
                    try {
                        int color = Color.parseColor(navigationBarColor);
                        window.setNavigationBarColor(color);
                        Logger.info(TAG, "Navigation bar color set to: " + navigationBarColor);
                    } catch (IllegalArgumentException e) {
                        Logger.error(TAG, "Invalid navigation bar color: " + navigationBarColor, e);
                    }
                }
            }
        });
    }

    /**
     * Set system bar appearance (light/dark icons)
     */
    public void setSystemBarAppearance(String statusBarStyle, String navigationBarStyle) {
        activity.runOnUiThread(() -> {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+) - Use WindowInsetsController
                WindowInsetsController controller = window.getInsetsController();
                if (controller != null) {
                    if (statusBarStyle != null) {
                        boolean lightStatusBar = "dark".equals(statusBarStyle);
                        controller.setSystemBarsAppearance(
                            lightStatusBar ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        );
                        Logger.info(TAG, "Status bar style set to: " + statusBarStyle);
                    }
                    
                    if (navigationBarStyle != null) {
                        boolean lightNavigationBar = "dark".equals(navigationBarStyle);
                        controller.setSystemBarsAppearance(
                            lightNavigationBar ? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                        Logger.info(TAG, "Navigation bar style set to: " + navigationBarStyle);
                    }
                }
            } else {
                // Android 10 and below - Use WindowInsetsControllerCompat
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);
                if (controller != null) {
                    if (statusBarStyle != null) {
                        boolean lightStatusBar = "dark".equals(statusBarStyle);
                        controller.setAppearanceLightStatusBars(lightStatusBar);
                        Logger.info(TAG, "Status bar style set to: " + statusBarStyle + " (compat)");
                    }
                    
                    if (navigationBarStyle != null) {
                        boolean lightNavigationBar = "dark".equals(navigationBarStyle);
                        controller.setAppearanceLightNavigationBars(lightNavigationBar);
                        Logger.info(TAG, "Navigation bar style set to: " + navigationBarStyle + " (compat)");
                    }
                }
            }
        });
    }

    /**
     * Get system bar insets (for safe area handling)
     */
    public JSObject getSystemBarInsets() {
        JSObject result = new JSObject();
        
        View decorView = activity.getWindow().getDecorView();
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(decorView);
        
        if (windowInsets != null) {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            
            result.put("statusBar", statusBars.top);
            result.put("navigationBar", navigationBars.bottom);
            result.put("top", systemBars.top);
            result.put("bottom", systemBars.bottom);
            result.put("left", systemBars.left);
            result.put("right", systemBars.right);
            
            Logger.info(TAG, "System bar insets: " + result.toString());
        } else {
            // Fallback values
            result.put("statusBar", 0);
            result.put("navigationBar", 0);
            result.put("top", 0);
            result.put("bottom", 0);
            result.put("left", 0);
            result.put("right", 0);
            
            Logger.warn(TAG, "Could not get window insets, returning zeros");
        }
        
        return result;
    }

    /**
     * Get keyboard information (height and visibility)
     * Works on Android 11+ (API 30+) with IME insets
     * Returns height in DIP (device independent pixels) like @capacitor/keyboard
     */
    public JSObject getKeyboardInfo() {
        JSObject result = new JSObject();
        
        View decorView = activity.getWindow().getDecorView();
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(decorView);
        
        // Get display metrics for DIP conversion
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        final float density = dm.density;
        
        if (windowInsets != null) {
            // Check IME (keyboard) visibility
            boolean imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
            
            // Get IME height in pixels
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int imeHeightPx = imeInsets.bottom;
            
            // Convert to DIP (device independent pixels)
            int imeHeightDip = Math.round(imeHeightPx / density);
            
            result.put("keyboardHeight", imeHeightDip);
            result.put("isVisible", imeVisible);
            
            Logger.info(TAG, "Keyboard info - Height: " + imeHeightDip + " dp (" + imeHeightPx + " px), Visible: " + imeVisible);
        } else {
            // Fallback
            result.put("keyboardHeight", 0);
            result.put("isVisible", false);
            
            Logger.warn(TAG, "Could not get keyboard info, returning defaults");
        }
        
        return result;
    }

    /**
     * Setup keyboard insets listener
     * Based on official @capacitor/keyboard implementation
     * Uses WindowInsetsAnimationCompat for proper keyboard event handling
     */
    public void setupKeyboardListener(KeyboardListener listener) {
        FrameLayout content = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        View rootView = content.getRootView();
        
        // Get display metrics for DIP conversion
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        final float density = dm.density;
        
        // Setup WindowInsetsAnimationCompat callback (like official @capacitor/keyboard)
        ViewCompat.setWindowInsetsAnimationCallback(
            rootView,
            new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
                @NonNull
                @Override
                public WindowInsetsCompat onProgress(
                    @NonNull WindowInsetsCompat insets,
                    @NonNull List<WindowInsetsAnimationCompat> runningAnimations
                ) {
                    return insets;
                }
                
                @NonNull
                @Override
                public WindowInsetsAnimationCompat.BoundsCompat onStart(
                    @NonNull WindowInsetsAnimationCompat animation,
                    @NonNull WindowInsetsAnimationCompat.BoundsCompat bounds
                ) {
                    WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(rootView);
                    if (insets == null) {
                        return super.onStart(animation, bounds);
                    }
                    
                    boolean showingKeyboard = insets.isVisible(WindowInsetsCompat.Type.ime());
                    int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    
                    // Convert to DIP (device independent pixels) like official plugin
                    int keyboardHeightInDip = Math.round(imeHeight / density);
                    
                    if (listener != null) {
                        listener.onKeyboardWillShow(keyboardHeightInDip, showingKeyboard);
                    }
                    
                    Logger.info(TAG, "Keyboard will " + (showingKeyboard ? "show" : "hide") + 
                               " - Height: " + keyboardHeightInDip + " dp (" + imeHeight + " px)");
                    
                    return super.onStart(animation, bounds);
                }
                
                @Override
                public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                    super.onEnd(animation);
                    
                    WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(rootView);
                    if (insets == null) {
                        return;
                    }
                    
                    boolean showingKeyboard = insets.isVisible(WindowInsetsCompat.Type.ime());
                    int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    
                    // Convert to DIP (device independent pixels) like official plugin
                    int keyboardHeightInDip = Math.round(imeHeight / density);
                    
                    if (listener != null) {
                        listener.onKeyboardDidShow(keyboardHeightInDip, showingKeyboard);
                    }
                    
                    Logger.info(TAG, "Keyboard did " + (showingKeyboard ? "show" : "hide") + 
                               " - Height: " + keyboardHeightInDip + " dp (" + imeHeight + " px)");
                }
            }
        );
        
        Logger.info(TAG, "Keyboard listener setup complete (Capacitor official style)");
    }

    /**
     * Interface for keyboard change callbacks
     * Matches @capacitor/keyboard event structure
     */
    public interface KeyboardListener {
        /**
         * Called when keyboard will show/hide (animation start)
         * @param height keyboard height in DIP (device independent pixels)
         * @param isVisible whether keyboard is becoming visible
         */
        void onKeyboardWillShow(int height, boolean isVisible);
        
        /**
         * Called when keyboard did show/hide (animation end)
         * @param height keyboard height in DIP (device independent pixels)  
         * @param isVisible whether keyboard is visible
         */
        void onKeyboardDidShow(int height, boolean isVisible);
    }
}
