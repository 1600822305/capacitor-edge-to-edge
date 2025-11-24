/**
 * Capacitor Edge-to-Edge Plugin
 * Provides native edge-to-edge display control for Android 11-16 (API 30-36)
 */
export interface EdgeToEdgePlugin {
  /**
   * Enable edge-to-edge mode (content draws behind system bars)
   * Supported on Android 11-16 (API 30-36)
   */
  enable(): Promise<void>;

  /**
   * Disable edge-to-edge mode (content below system bars)
   */
  disable(): Promise<void>;

  /**
   * Set system bars to transparent
   * @param options Configuration for transparent bars
   */
  setTransparentSystemBars(options: TransparentBarsOptions): Promise<void>;

  /**
   * Set system bar colors
   * @param options Color configuration
   */
  setSystemBarColors(options: SystemBarColorsOptions): Promise<void>;

  /**
   * Set system bar appearance (light/dark icons)
   * @param options Appearance configuration
   */
  setSystemBarAppearance(options: SystemBarAppearanceOptions): Promise<void>;

  /**
   * Get current system bar insets (for safe area handling)
   * @returns Inset values in pixels
   */
  getSystemBarInsets(): Promise<SystemBarInsetsResult>;

  /**
   * Get current keyboard height and visibility
   * @returns Keyboard information
   */
  getKeyboardInfo(): Promise<KeyboardInfo>;

  /**
   * Show the keyboard
   * 
   * Note: This method is alpha and may have issues.
   * - Android: Supported
   * - iOS: Not supported (will call unimplemented)
   * - Web: Not supported
   * 
   * @since 1.6.0
   */
  show(): Promise<void>;

  /**
   * Hide the keyboard
   * 
   * @since 1.6.0
   */
  hide(): Promise<void>;

  /**
   * Add listener for keyboard events
   * @param eventName - Event name
   * @param listenerFunc - Callback function
   */
  addListener(
    eventName: 'keyboardWillShow' | 'keyboardDidShow' | 'keyboardWillHide' | 'keyboardDidHide',
    listenerFunc: (event: KeyboardEvent) => void
  ): Promise<PluginListenerHandle>;

  /**
   * Remove all listeners for this plugin
   */
  removeAllListeners(): Promise<void>;
}

export interface TransparentBarsOptions {
  /**
   * Make status bar transparent
   * @default true
   */
  statusBar?: boolean;

  /**
   * Make navigation bar transparent
   * @default true
   */
  navigationBar?: boolean;
}

export interface SystemBarColorsOptions {
  /**
   * Status bar background color (hex format: #RRGGBB or #AARRGGBB)
   * @example "#FF5733" or "#80FF5733"
   */
  statusBarColor?: string;

  /**
   * Navigation bar background color (hex format: #RRGGBB or #AARRGGBB)
   * @example "#000000" or "#80000000"
   */
  navigationBarColor?: string;
}

export interface SystemBarAppearanceOptions {
  /**
   * Status bar icon/text appearance
   * - "light": Light icons/text (for dark backgrounds)
   * - "dark": Dark icons/text (for light backgrounds)
   */
  statusBarStyle?: 'light' | 'dark';

  /**
   * Navigation bar button appearance
   * - "light": Light buttons (for dark backgrounds)
   * - "dark": Dark buttons (for light backgrounds)
   */
  navigationBarStyle?: 'light' | 'dark';
}

export interface PluginListenerHandle {
  remove: () => Promise<void>;
}

export interface KeyboardInfo {
  /**
   * Keyboard height
   * - Android: in DP units
   * - iOS: in pixels
   * - Web: estimated in pixels
   */
  keyboardHeight: number;

  /**
   * Whether keyboard is currently visible
   */
  isVisible: boolean;
}

export interface KeyboardEvent {
  /**
   * Keyboard height
   * - Android: in DP units (density-independent pixels)
   * - iOS: in pixels
   * - Web: estimated in pixels
   *
   * Compatible with official @capacitor/keyboard plugin
   */
  keyboardHeight: number;
}

export interface SystemBarInsetsResult {
  /**
   * Status bar height in pixels
   */
  statusBar: number;

  /**
   * Navigation bar height in pixels
   */
  navigationBar: number;

  /**
   * Top safe area inset
   */
  top: number;

  /**
   * Bottom safe area inset
   */
  bottom: number;

  /**
   * Left safe area inset (for landscape/foldables)
   */
  left: number;

  /**
   * Right safe area inset (for landscape/foldables)
   */
  right: number;
}
