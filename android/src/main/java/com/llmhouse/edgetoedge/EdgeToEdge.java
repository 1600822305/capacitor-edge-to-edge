package com.llmhouse.edgetoedge;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
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
     */
    public JSObject getKeyboardInfo() {
        JSObject result = new JSObject();
        
        View decorView = activity.getWindow().getDecorView();
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(decorView);
        
        if (windowInsets != null) {
            // Check IME (keyboard) visibility
            boolean imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
            
            // Get IME height
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int imeHeight = imeInsets.bottom;
            
            result.put("height", imeHeight);
            result.put("isVisible", imeVisible);
            
            Logger.info(TAG, "Keyboard info - Height: " + imeHeight + "px, Visible: " + imeVisible);
        } else {
            // Fallback
            result.put("height", 0);
            result.put("isVisible", false);
            
            Logger.warn(TAG, "Could not get keyboard info, returning defaults");
        }
        
        return result;
    }

    /**
     * Setup keyboard insets listener
     * This will notify when keyboard shows/hides
     * Improved with deduplication logic (Flutter-style)
     * Uses WindowInsetsAnimation.Callback on Android 11+ for smooth animations
     */
    public void setupKeyboardListener(KeyboardListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+): Use WindowInsetsAnimation for smooth frame-by-frame updates
            setupKeyboardAnimationListener(listener);
        } else {
            // Android 10 and below: Use OnApplyWindowInsetsListener
            setupLegacyKeyboardListener(listener);
        }
    }
    
    /**
     * Setup keyboard animation listener for Android 11+ (API 30+)
     * Provides smooth frame-by-frame updates during keyboard animation
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void setupKeyboardAnimationListener(KeyboardListener listener) {
        View decorView = activity.getWindow().getDecorView();
        
        // Track previous state to avoid duplicate events
        final int[] lastHeight = {0};
        final boolean[] lastVisible = {false};
        
        // Create animation callback for smooth keyboard animations
        WindowInsetsAnimation.Callback animationCallback = new WindowInsetsAnimation.Callback(
            WindowInsetsAnimation.Callback.DISPATCH_MODE_STOP
        ) {
            @NonNull
            @Override
            public WindowInsets onProgress(@NonNull WindowInsets insets, 
                                          @NonNull List<WindowInsetsAnimation> runningAnimations) {
                // Called on every frame of the keyboard animation
                Insets imeInsets = insets.getInsets(WindowInsets.Type.ime());
                boolean imeVisible = insets.isVisible(WindowInsets.Type.ime());
                int imeHeight = imeInsets.bottom;
                
                // Check if this is an IME animation
                boolean isImeAnimation = false;
                for (WindowInsetsAnimation animation : runningAnimations) {
                    if ((animation.getTypeMask() & WindowInsets.Type.ime()) != 0) {
                        isImeAnimation = true;
                        break;
                    }
                }
                
                // Only notify for IME-related changes
                if (isImeAnimation || imeHeight != lastHeight[0] || imeVisible != lastVisible[0]) {
                    lastHeight[0] = imeHeight;
                    lastVisible[0] = imeVisible;
                    
                    if (listener != null) {
                        listener.onKeyboardChanged(imeHeight, imeVisible);
                    }
                }
                
                return insets;
            }
            
            @Override
            public void onEnd(@NonNull WindowInsetsAnimation animation) {
                // Animation completed
                if ((animation.getTypeMask() & WindowInsets.Type.ime()) != 0) {
                    Logger.info(TAG, "Keyboard animation completed");
                }
                super.onEnd(animation);
            }
            
            @Override
            public void onPrepare(@NonNull WindowInsetsAnimation animation) {
                // Animation about to start
                if ((animation.getTypeMask() & WindowInsets.Type.ime()) != 0) {
                    Logger.info(TAG, "Keyboard animation starting");
                }
                super.onPrepare(animation);
            }
        };
        
        // Set the animation callback
        decorView.setWindowInsetsAnimationCallback(animationCallback);
        
        // Also set OnApplyWindowInsetsListener for initial state
        ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int imeHeight = imeInsets.bottom;
            
            if (imeHeight != lastHeight[0] || imeVisible != lastVisible[0]) {
                lastHeight[0] = imeHeight;
                lastVisible[0] = imeVisible;
                
                if (listener != null) {
                    listener.onKeyboardChanged(imeHeight, imeVisible);
                }
                
                Logger.info(TAG, "Keyboard state (API 30+) - Height: " + imeHeight + "px, Visible: " + imeVisible);
            }
            
            return insets;
        });
        
        Logger.info(TAG, "Keyboard animation listener setup complete (Android 11+ with smooth animations)");
    }
    
    /**
     * Setup legacy keyboard listener for Android 10 and below
     * Uses OnApplyWindowInsetsListener only
     */
    private void setupLegacyKeyboardListener(KeyboardListener listener) {
        View decorView = activity.getWindow().getDecorView();
        
        // Track previous state to avoid duplicate events
        final int[] lastHeight = {0};
        final boolean[] lastVisible = {false};
        
        ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
            // Get IME (keyboard) insets
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int imeHeight = imeInsets.bottom;
            
            // Only notify if state actually changed (avoid duplicate events)
            boolean heightChanged = imeHeight != lastHeight[0];
            boolean visibilityChanged = imeVisible != lastVisible[0];
            
            if (heightChanged || visibilityChanged) {
                // Update tracked state
                lastHeight[0] = imeHeight;
                lastVisible[0] = imeVisible;
                
                // Notify listener
                if (listener != null) {
                    listener.onKeyboardChanged(imeHeight, imeVisible);
                }
                
                Logger.info(TAG, "Keyboard state changed (Legacy) - Height: " + imeHeight + "px, Visible: " + imeVisible);
            }
            
            // IMPORTANT: Always return insets for proper consumption
            return insets;
        });
        
        Logger.info(TAG, "Legacy keyboard listener setup complete (with deduplication)");
    }

    /**
     * Interface for keyboard change callbacks
     */
    public interface KeyboardListener {
        void onKeyboardChanged(int height, boolean isVisible);
    }
}
