import { WebPlugin } from '@capacitor/core';

import type {
  EdgeToEdgePlugin,
  TransparentBarsOptions,
  SystemBarColorsOptions,
  SystemBarAppearanceOptions,
  SystemBarInsetsResult,
} from './definitions';

export class EdgeToEdgeWeb extends WebPlugin implements EdgeToEdgePlugin {
  private keyboardHeight = 0;
  private keyboardVisible = false;

  constructor() {
    super();
    this.setupKeyboardListener();
  }

  async enable(): Promise<void> {
    console.log('[EdgeToEdge Web] Enable called - no-op on web platform');
    // Web doesn't support true edge-to-edge, but we can update meta tags
    this.updateMetaTags();
  }

  async disable(): Promise<void> {
    console.log('[EdgeToEdge Web] Disable called - no-op on web platform');
  }

  async setTransparentSystemBars(_options: TransparentBarsOptions): Promise<void> {
    console.log('[EdgeToEdge Web] setTransparentSystemBars called - no-op on web platform');
  }

  async setSystemBarColors(options: SystemBarColorsOptions): Promise<void> {
    console.log('[EdgeToEdge Web] setSystemBarColors called:', options);
    
    // Update theme-color meta tag for mobile browsers
    if (options.statusBarColor) {
      this.updateThemeColor(options.statusBarColor);
    }
  }

  async setSystemBarAppearance(options: SystemBarAppearanceOptions): Promise<void> {
    console.log('[EdgeToEdge Web] setSystemBarAppearance called:', options);
    
    // Update apple-mobile-web-app-status-bar-style for iOS Safari
    if (options.statusBarStyle) {
      this.updateAppleStatusBarStyle(options.statusBarStyle);
    }
  }

  async getSystemBarInsets(): Promise<SystemBarInsetsResult> {
    // Return safe area insets from CSS env() if available
    const top = this.getSafeAreaInset('top');
    const bottom = this.getSafeAreaInset('bottom');
    const left = this.getSafeAreaInset('left');
    const right = this.getSafeAreaInset('right');
    
    return {
      statusBar: top,
      navigationBar: bottom,
      top,
      bottom,
      left,
      right,
    };
  }

  // Helper methods
  private updateMetaTags(): void {
    // Add viewport-fit=cover for iOS notch support
    let viewport = document.querySelector('meta[name="viewport"]');
    if (viewport) {
      const content = viewport.getAttribute('content') || '';
      if (!content.includes('viewport-fit')) {
        viewport.setAttribute('content', content + ', viewport-fit=cover');
      }
    }
  }

  private updateThemeColor(color: string): void {
    let meta = document.querySelector('meta[name="theme-color"]') as HTMLMetaElement;
    if (!meta) {
      meta = document.createElement('meta');
      meta.name = 'theme-color';
      document.head.appendChild(meta);
    }
    meta.content = color;
  }

  private updateAppleStatusBarStyle(style: string): void {
    let meta = document.querySelector('meta[name="apple-mobile-web-app-status-bar-style"]') as HTMLMetaElement;
    if (!meta) {
      meta = document.createElement('meta');
      meta.name = 'apple-mobile-web-app-status-bar-style';
      document.head.appendChild(meta);
    }
    // Map our style to iOS values: default, black, black-translucent
    meta.content = style === 'light' ? 'default' : 'black';
  }

  private getSafeAreaInset(position: 'top' | 'bottom' | 'left' | 'right'): number {
    // Try to get safe area inset from CSS env()
    const value = getComputedStyle(document.documentElement)
      .getPropertyValue(`--safe-area-inset-${position}`);
    
    if (value) {
      return parseInt(value, 10) || 0;
    }
    
    // Fallback: check for env() support
    const testDiv = document.createElement('div');
    testDiv.style.paddingTop = `env(safe-area-inset-${position})`;
    document.body.appendChild(testDiv);
    const padding = parseInt(getComputedStyle(testDiv).paddingTop, 10);
    document.body.removeChild(testDiv);
    
    return padding || 0;
  }

  async getKeyboardInfo(): Promise<{ height: number; isVisible: boolean }> {
    return {
      height: this.keyboardHeight,
      isVisible: this.keyboardVisible,
    };
  }

  private setupKeyboardListener(): void {
    // Use Visual Viewport API to detect keyboard on mobile web
    if (window.visualViewport) {
      let lastHeight = window.visualViewport.height;

      window.visualViewport.addEventListener('resize', () => {
        const currentHeight = window.visualViewport!.height;
        const heightDiff = lastHeight - currentHeight;

        // Keyboard is likely showing if viewport height decreased significantly
        if (heightDiff > 100) {
          this.keyboardHeight = heightDiff;
          this.keyboardVisible = true;

          this.notifyListeners('keyboardWillShow', {
            height: heightDiff,
            isVisible: true,
          });

          this.notifyListeners('keyboardDidShow', {
            height: heightDiff,
            isVisible: true,
          });
        }
        // Keyboard is likely hiding if viewport height increased
        else if (heightDiff < -100) {
          this.keyboardHeight = 0;
          this.keyboardVisible = false;

          this.notifyListeners('keyboardWillHide', {
            height: 0,
            isVisible: false,
          });

          this.notifyListeners('keyboardDidHide', {
            height: 0,
            isVisible: false,
          });
        }

        lastHeight = currentHeight;
      });
    } else {
      // Fallback for browsers without Visual Viewport API
      console.log('[EdgeToEdge Web] Visual Viewport API not available - keyboard detection limited');
    }
  }
}
