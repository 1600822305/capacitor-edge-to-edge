package com.llmhouse.edgetoedge;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import java.util.List;

import com.getcapacitor.Logger;
import com.getcapacitor.JSObject;

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
     * Returns height in DP units for consistency with official Capacitor Keyboard plugin
     */
    public JSObject getKeyboardInfo() {
        JSObject result = new JSObject();
        
        View decorView = activity.getWindow().getDecorView();
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(decorView);
        
        if (windowInsets != null) {
            // Check IME (keyboard) visibility
            boolean imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
            
            // Get IME height in pixels
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int imeHeightPx = imeInsets.bottom;
            
            // Convert to DP for consistency
            DisplayMetrics dm = activity.getResources().getDisplayMetrics();
            final float density = dm.density;
            int imeHeightDp = Math.round(imeHeightPx / density);
            
            result.put("keyboardHeight", imeHeightDp);
            result.put("isVisible", imeVisible);
            
            Logger.info(TAG, "Keyboard info - Height: " + imeHeightDp + "dp (" + imeHeightPx + "px), Visible: " + imeVisible);
        } else {
            // Fallback
            result.put("keyboardHeight", 0);
            result.put("isVisible", false);
            
            Logger.warn(TAG, "Could not get keyboard info, returning defaults");
        }
        
        return result;
    }

    /**
     * Setup keyboard animation listener (official Capacitor Keyboard plugin approach)
     * Uses WindowInsetsAnimationCompat.Callback for precise animation tracking
     * This will notify when keyboard shows/hides with proper will/did events
     */
    public void setupKeyboardListener(KeyboardListener listener) {
        FrameLayout content = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        View rootView = content.getRootView();
        
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
                    boolean showingKeyboard = ViewCompat.getRootWindowInsets(rootView).isVisible(WindowInsetsCompat.Type.ime());
                    WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(rootView);
                    int imeHeightPx = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    
                    // Convert to DP
                    DisplayMetrics dm = activity.getResources().getDisplayMetrics();
                    final float density = dm.density;
                    int imeHeightDp = Math.round(imeHeightPx / density);

                    if (listener != null) {
                        if (showingKeyboard) {
                            listener.onKeyboardWillShow(imeHeightDp);
                        } else {
                            listener.onKeyboardWillHide();
                        }
                    }
                    return super.onStart(animation, bounds);
                }

                @Override
                public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                    super.onEnd(animation);
                    boolean showingKeyboard = ViewCompat.getRootWindowInsets(rootView).isVisible(WindowInsetsCompat.Type.ime());
                    WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(rootView);
                    int imeHeightPx = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    
                    // Convert to DP
                    DisplayMetrics dm = activity.getResources().getDisplayMetrics();
                    final float density = dm.density;
                    int imeHeightDp = Math.round(imeHeightPx / density);

                    if (listener != null) {
                        if (showingKeyboard) {
                            listener.onKeyboardDidShow(imeHeightDp);
                        } else {
                            listener.onKeyboardDidHide();
                        }
                    }
                }
            }
        );
        
        Logger.info(TAG, "Keyboard animation listener setup complete (Capacitor Keyboard official approach)");
    }

    /**
     * Show the keyboard (Android only)
     * Based on official Capacitor Keyboard plugin
     */
    public void showKeyboard() {
        activity.runOnUiThread(() -> {
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(currentFocus, 0);
                Logger.info(TAG, "Keyboard show requested");
            } else {
                Logger.warn(TAG, "Cannot show keyboard - no focused view");
            }
        });
    }

    /**
     * Hide the keyboard
     * Based on official Capacitor Keyboard plugin
     */
    public boolean hideKeyboard() {
        final boolean[] result = {false};
        activity.runOnUiThread(() -> {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus == null) {
                Logger.warn(TAG, "Cannot hide keyboard - no focused view");
                result[0] = false;
            } else {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Logger.info(TAG, "Keyboard hide requested");
                result[0] = true;
            }
        });
        return result[0];
    }

    /**
     * Interface for keyboard change callbacks
     * Based on official Capacitor Keyboard plugin events
     */
    public interface KeyboardListener {
        void onKeyboardWillShow(int keyboardHeight);
        void onKeyboardDidShow(int keyboardHeight);
        void onKeyboardWillHide();
        void onKeyboardDidHide();
    }
}
