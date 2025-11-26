# capacitor-edge-to-edge

A Capacitor plugin for implementing Android edge-to-edge display with native control over system bars (status bar and navigation bar).

## Features

✅ **Native Edge-to-Edge Mode** - Content draws behind system bars  
✅ **Transparent System Bars** - Full control over transparency  
✅ **Custom Bar Colors** - Set any color with alpha support  
✅ **Light/Dark Icons** - Control icon appearance based on background  
✅ **Safe Area Insets** - Get system bar sizes for layout adjustment  
✅ **Keyboard Detection** - Get keyboard height and listen to show/hide events  
✅ **Android 11-16 Support** - Compatible with API 30-36  
✅ **iOS 14+ Support** - Status bar control and safe area handling  
✅ **Web Platform Support** - Fallback implementation for web/PWA  

## Install

```bash
npm install capacitor-edge-to-edge
npx cap sync
```

## Usage

### Basic Setup

```typescript
import { EdgeToEdge } from 'capacitor-edge-to-edge';

// Enable edge-to-edge mode
await EdgeToEdge.enable();

// Make system bars transparent
await EdgeToEdge.setTransparentSystemBars({
  statusBar: true,
  navigationBar: true
});

// Set dark icons for light backgrounds
await EdgeToEdge.setSystemBarAppearance({
  statusBarStyle: 'dark',
  navigationBarStyle: 'dark'
});
```

### Complete Example

```typescript
import { EdgeToEdge } from 'capacitor-edge-to-edge';

async function setupEdgeToEdge() {
  try {
    // 1. Enable edge-to-edge mode
    await EdgeToEdge.enable();
    
    // 2. Set transparent system bars
    await EdgeToEdge.setTransparentSystemBars({
      statusBar: true,
      navigationBar: true
    });
    
    // 3. Configure appearance (light icons for dark backgrounds)
    await EdgeToEdge.setSystemBarAppearance({
      statusBarStyle: 'light',
      navigationBarStyle: 'light'
    });
    
    // 4. Get system bar insets for safe area handling
    const insets = await EdgeToEdge.getSystemBarInsets();
    console.log('Status bar height:', insets.statusBar);
    console.log('Navigation bar height:', insets.navigationBar);
    
    // Apply padding to your content
    document.body.style.paddingTop = `${insets.top}px`;
    document.body.style.paddingBottom = `${insets.bottom}px`;
    
  } catch (error) {
    console.error('Edge-to-edge setup failed:', error);
  }
}
```

### Keyboard Handling

```typescript
import { EdgeToEdge } from 'capacitor-edge-to-edge';

// Get current keyboard state
const keyboardInfo = await EdgeToEdge.getKeyboardInfo();
console.log('Keyboard height:', keyboardInfo.height);
console.log('Is visible:', keyboardInfo.isVisible);

// Listen to keyboard events
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  console.log('Keyboard showing, height:', event.height);
  
  // Adjust your input container
  const inputContainer = document.querySelector('.input-container');
  inputContainer.style.transform = `translateY(-${event.height}px)`;
});

EdgeToEdge.addListener('keyboardWillHide', () => {
  console.log('Keyboard hiding');
  
  // Reset position
  const inputContainer = document.querySelector('.input-container');
  inputContainer.style.transform = 'translateY(0)';
});

// Clean up when done
EdgeToEdge.removeAllListeners();
```

For more keyboard examples, see [KEYBOARD_GUIDE.md](KEYBOARD_GUIDE.md).

### Theme Integration

```typescript
// Light mode
await EdgeToEdge.setSystemBarColors({
  statusBarColor: '#FFFFFF',
  navigationBarColor: '#FFFFFF'
});
await EdgeToEdge.setSystemBarAppearance({
  statusBarStyle: 'dark',  // Dark icons on light background
  navigationBarStyle: 'dark'
});

// Dark mode
await EdgeToEdge.setSystemBarColors({
  statusBarColor: '#000000',
  navigationBarColor: '#000000'
});
await EdgeToEdge.setSystemBarAppearance({
  statusBarStyle: 'light',  // Light icons on dark background
  navigationBarStyle: 'light'
});

// Semi-transparent overlay
await EdgeToEdge.setSystemBarColors({
  statusBarColor: '#80000000',  // 50% transparent black
  navigationBarColor: '#80000000'
});
```

### React/Vue/Angular Integration

```typescript
// React Hook Example
import { useEffect } from 'react';
import { EdgeToEdge } from 'capacitor-edge-to-edge';

function useEdgeToEdge(theme: 'light' | 'dark') {
  useEffect(() => {
    const setupBars = async () => {
      await EdgeToEdge.enable();
      await EdgeToEdge.setTransparentSystemBars({ 
        statusBar: true, 
        navigationBar: true 
      });
      
      const style = theme === 'dark' ? 'light' : 'dark';
      await EdgeToEdge.setSystemBarAppearance({
        statusBarStyle: style,
        navigationBarStyle: style
      });
    };
    
    setupBars();
  }, [theme]);
}
```

## API

### `enable()`

Enable edge-to-edge mode. Content will draw behind system bars.

```typescript
await EdgeToEdge.enable();
```

### `disable()`

Disable edge-to-edge mode. Content will be below system bars.

```typescript
await EdgeToEdge.disable();
```

### `setTransparentSystemBars(options)`

Set system bars to transparent.

```typescript
await EdgeToEdge.setTransparentSystemBars({
  statusBar: true,      // Make status bar transparent
  navigationBar: true   // Make navigation bar transparent
});
```

### `setSystemBarColors(options)`

Set custom colors for system bars. Supports hex colors with alpha channel.

```typescript
await EdgeToEdge.setSystemBarColors({
  statusBarColor: '#FF5733',      // Solid color
  navigationBarColor: '#80000000' // 50% transparent black
});
```

### `setSystemBarAppearance(options)`

Control icon/text appearance on system bars.

```typescript
await EdgeToEdge.setSystemBarAppearance({
  statusBarStyle: 'light',      // 'light' | 'dark'
  navigationBarStyle: 'light'   // 'light' | 'dark'
});
```

**Note**: 
- `'light'` = Light icons (use for dark backgrounds)
- `'dark'` = Dark icons (use for light backgrounds)

### `getSystemBarInsets()`

Get system bar sizes for safe area handling.

```typescript
const insets = await EdgeToEdge.getSystemBarInsets();
// Returns: {
//   statusBar: number,        // Status bar height
//   navigationBar: number,    // Navigation bar height
//   top: number,             // Top safe area
//   bottom: number,          // Bottom safe area
//   left: number,            // Left safe area
//   right: number            // Right safe area
// }
```

### `getKeyboardInfo()`

Get current keyboard height and visibility.

```typescript
const keyboardInfo = await EdgeToEdge.getKeyboardInfo();
// Returns: {
//   height: number,          // Keyboard height in pixels
//   isVisible: boolean       // Whether keyboard is visible
// }
```

### Keyboard Event Listeners

Listen to keyboard show/hide events.

```typescript
// Available events:
await EdgeToEdge.addListener('keyboardWillShow', (event) => {
  // event.height: number
  // event.isVisible: boolean
  // event.animationDuration?: number (iOS only)
});

await EdgeToEdge.addListener('keyboardWillHide', (event) => {
  // Fired when keyboard starts hiding
});

await EdgeToEdge.addListener('keyboardDidShow', (event) => {
  // Fired when keyboard animation completes
});

await EdgeToEdge.addListener('keyboardDidHide', (event) => {
  // Fired when keyboard is fully hidden
});

// Remove all listeners
await EdgeToEdge.removeAllListeners();
```

## Platform Support

| Platform | Status | Notes |
|----------|--------|-------|
| **Android 11-16** | ✅ Full Support | Native edge-to-edge with WindowInsetsController (API 30-36) |
| **Android 10** | ✅ Compat Mode | Using WindowInsetsControllerCompat |
| **iOS 14+** | ✅ Full Support | Status bar appearance and safe area insets |
| **Web** | ⚠️ Limited | Meta tags and CSS safe-area-inset |

### Android 16 (API 36) Notes

**Important Changes:**
- Android 16 **fully enforces** edge-to-edge mode for apps targeting API 36
- The `windowOptOutEdgeToEdgeEnforcement` attribute is **deprecated and disabled**
- Apps can no longer opt-out of edge-to-edge display
- This plugin is fully compatible with Android 16's enforcement requirements

If you're migrating from Android 15 (API 35) to Android 16 (API 36):
1. Remove any `windowOptOutEdgeToEdgeEnforcement` from your theme
2. Ensure proper inset handling using `getSystemBarInsets()`
3. Test your app layout with system bars overlaying content

### iOS 14+ Notes

**What's Supported:**
- ✅ Status bar appearance (light/dark content)
- ✅ Safe area insets detection (including notch and home indicator)
- ✅ Background color changes
- ⚠️ Status bar color is always transparent (iOS limitation)

**iOS-Specific Configuration:**

Add to your `Info.plist` for status bar control:
```xml
<key>UIViewControllerBasedStatusBarAppearance</key>
<true/>
```

For edge-to-edge content on iOS:
```swift
// Content extends under status bar automatically
// Use safe area insets to adjust your layout
```

**Platform Differences:**
- iOS status bar is always transparent (system design)
- Navigation bar refers to home indicator on iOS (not a top bar)
- Safe area includes notch, Dynamic Island, and home indicator

## CSS Safe Area Support

Add to your global CSS for proper safe area handling:

```css
body {
  padding-top: env(safe-area-inset-top);
  padding-bottom: env(safe-area-inset-bottom);
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
}

/* Or use CSS variables */
:root {
  --safe-area-top: env(safe-area-inset-top, 0px);
  --safe-area-bottom: env(safe-area-inset-bottom, 0px);
}
```

## Android Configuration

No additional configuration needed! The plugin handles everything automatically.

## Comparison with Other Solutions

| Feature | This Plugin | @capacitor/status-bar | @capawesome/edge-to-edge |
|---------|-------------|----------------------|-------------------------|
| Edge-to-edge mode | ✅ | ❌ | ✅ |
| Transparent bars | ✅ | ⚠️ Limited | ✅ |
| Custom colors | ✅ | ✅ | ✅ |
| Icon appearance | ✅ | ✅ | ⚠️ Limited |
| Inset detection | ✅ | ❌ | ⚠️ Limited |
| Android 11-16 (API 30-36) | ✅ | ✅ | ✅ |
| iOS 14+ | ✅ | ✅ | ⚠️ Limited |

## Troubleshooting

### Content appears behind system bars

This is expected in edge-to-edge mode. Use `getSystemBarInsets()` to add appropriate padding:

```typescript
const insets = await EdgeToEdge.getSystemBarInsets();
document.body.style.paddingTop = `${insets.top}px`;
```

### System bars not transparent

Make sure to call both `enable()` and `setTransparentSystemBars()`:

```typescript
await EdgeToEdge.enable();
await EdgeToEdge.setTransparentSystemBars({ 
  statusBar: true, 
  navigationBar: true 
});
```

### iOS status bar not changing style

Ensure `UIViewControllerBasedStatusBarAppearance` is set to `true` in `Info.plist`:

```xml
<key>UIViewControllerBasedStatusBarAppearance</key>
<true/>
```

### iOS safe area insets not correct

Make sure to call `getSystemBarInsets()` after the view has loaded:

```typescript
// In your component's mount/load lifecycle
useEffect(() => {
  const updateInsets = async () => {
    const insets = await EdgeToEdge.getSystemBarInsets();
    // Use insets
  };
  updateInsets();
}, []);
```

## License

MIT

## Credits

Created by [q1600822305](https://github.com/q1600822305)

<docgen-index>

* [`enable()`](#enable)
* [`disable()`](#disable)
* [`setTransparentSystemBars(...)`](#settransparentsystembars)
* [`setSystemBarColors(...)`](#setsystembarcolors)
* [`setSystemBarAppearance(...)`](#setsystembarappearance)
* [`getSystemBarInsets()`](#getsystembarinsets)
* [`getKeyboardInfo()`](#getkeyboardinfo)
* [`show()`](#show)
* [`hide()`](#hide)
* [`refreshSafeArea()`](#refreshsafearea)
* [`addListener('keyboardWillShow' | 'keyboardDidShow' | 'keyboardWillHide' | 'keyboardDidHide', ...)`](#addlistenerkeyboardwillshow--keyboarddidshow--keyboardwillhide--keyboarddidhide-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Capacitor Edge-to-Edge Plugin
Provides native edge-to-edge display control for Android 11-16 (API 30-36)

### enable()

```typescript
enable() => Promise<void>
```

Enable edge-to-edge mode (content draws behind system bars)
Supported on Android 11-16 (API 30-36)

--------------------


### disable()

```typescript
disable() => Promise<void>
```

Disable edge-to-edge mode (content below system bars)

--------------------


### setTransparentSystemBars(...)

```typescript
setTransparentSystemBars(options: TransparentBarsOptions) => Promise<void>
```

Set system bars to transparent

| Param         | Type                                                                      | Description                        |
| ------------- | ------------------------------------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#transparentbarsoptions">TransparentBarsOptions</a></code> | Configuration for transparent bars |

--------------------


### setSystemBarColors(...)

```typescript
setSystemBarColors(options: SystemBarColorsOptions) => Promise<void>
```

Set system bar colors

| Param         | Type                                                                      | Description         |
| ------------- | ------------------------------------------------------------------------- | ------------------- |
| **`options`** | <code><a href="#systembarcolorsoptions">SystemBarColorsOptions</a></code> | Color configuration |

--------------------


### setSystemBarAppearance(...)

```typescript
setSystemBarAppearance(options: SystemBarAppearanceOptions) => Promise<void>
```

Set system bar appearance (light/dark icons)

| Param         | Type                                                                              | Description              |
| ------------- | --------------------------------------------------------------------------------- | ------------------------ |
| **`options`** | <code><a href="#systembarappearanceoptions">SystemBarAppearanceOptions</a></code> | Appearance configuration |

--------------------


### getSystemBarInsets()

```typescript
getSystemBarInsets() => Promise<SystemBarInsetsResult>
```

Get current system bar insets (for safe area handling)

**Returns:** <code>Promise&lt;<a href="#systembarinsetsresult">SystemBarInsetsResult</a>&gt;</code>

--------------------


### getKeyboardInfo()

```typescript
getKeyboardInfo() => Promise<KeyboardInfo>
```

Get current keyboard height and visibility

**Returns:** <code>Promise&lt;<a href="#keyboardinfo">KeyboardInfo</a>&gt;</code>

--------------------


### show()

```typescript
show() => Promise<void>
```

Show the keyboard

Note: This method is alpha and may have issues.
- Android: Supported
- iOS: Not supported (will call unimplemented)
- Web: Not supported

**Since:** 1.6.0

--------------------


### hide()

```typescript
hide() => Promise<void>
```

Hide the keyboard

**Since:** 1.6.0

--------------------


### refreshSafeArea()

```typescript
refreshSafeArea() => Promise<void>
```

Refresh safe area CSS variables (useful after orientation change)
Injects --safe-area-inset-* CSS variables into the WebView

**Since:** 1.6.3

--------------------


### addListener('keyboardWillShow' | 'keyboardDidShow' | 'keyboardWillHide' | 'keyboardDidHide', ...)

```typescript
addListener(eventName: 'keyboardWillShow' | 'keyboardDidShow' | 'keyboardWillHide' | 'keyboardDidHide', listenerFunc: (event: KeyboardEvent) => void) => Promise<PluginListenerHandle>
```

Add listener for keyboard events

| Param              | Type                                                                                            | Description         |
| ------------------ | ----------------------------------------------------------------------------------------------- | ------------------- |
| **`eventName`**    | <code>'keyboardWillShow' \| 'keyboardDidShow' \| 'keyboardWillHide' \| 'keyboardDidHide'</code> | - Event name        |
| **`listenerFunc`** | <code>(event: <a href="#keyboardevent">KeyboardEvent</a>) =&gt; void</code>                     | - Callback function |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all listeners for this plugin

--------------------


### Interfaces


#### TransparentBarsOptions

| Prop                | Type                 | Description                     | Default           |
| ------------------- | -------------------- | ------------------------------- | ----------------- |
| **`statusBar`**     | <code>boolean</code> | Make status bar transparent     | <code>true</code> |
| **`navigationBar`** | <code>boolean</code> | Make navigation bar transparent | <code>true</code> |


#### SystemBarColorsOptions

| Prop                     | Type                | Description                                                        |
| ------------------------ | ------------------- | ------------------------------------------------------------------ |
| **`statusBarColor`**     | <code>string</code> | Status bar background color (hex format: #RRGGBB or #AARRGGBB)     |
| **`navigationBarColor`** | <code>string</code> | Navigation bar background color (hex format: #RRGGBB or #AARRGGBB) |


#### SystemBarAppearanceOptions

| Prop                     | Type                           | Description                                                                                                                          |
| ------------------------ | ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------ |
| **`statusBarStyle`**     | <code>'light' \| 'dark'</code> | Status bar icon/text appearance - "light": Light icons/text (for dark backgrounds) - "dark": Dark icons/text (for light backgrounds) |
| **`navigationBarStyle`** | <code>'light' \| 'dark'</code> | Navigation bar button appearance - "light": Light buttons (for dark backgrounds) - "dark": Dark buttons (for light backgrounds)      |


#### SystemBarInsetsResult

| Prop                | Type                | Description                                     |
| ------------------- | ------------------- | ----------------------------------------------- |
| **`statusBar`**     | <code>number</code> | Status bar height in pixels                     |
| **`navigationBar`** | <code>number</code> | Navigation bar height in pixels                 |
| **`top`**           | <code>number</code> | Top safe area inset                             |
| **`bottom`**        | <code>number</code> | Bottom safe area inset                          |
| **`left`**          | <code>number</code> | Left safe area inset (for landscape/foldables)  |
| **`right`**         | <code>number</code> | Right safe area inset (for landscape/foldables) |


#### KeyboardInfo

| Prop                 | Type                 | Description                                                                        |
| -------------------- | -------------------- | ---------------------------------------------------------------------------------- |
| **`keyboardHeight`** | <code>number</code>  | Keyboard height - Android: in DP units - iOS: in pixels - Web: estimated in pixels |
| **`isVisible`**      | <code>boolean</code> | Whether keyboard is currently visible                                              |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### KeyboardEvent

| Prop                 | Type                | Description                                                                                                                                                         |
| -------------------- | ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`keyboardHeight`** | <code>number</code> | Keyboard height - Android: in DP units (density-independent pixels) - iOS: in pixels - Web: estimated in pixels Compatible with official @capacitor/keyboard plugin |

</docgen-api>
