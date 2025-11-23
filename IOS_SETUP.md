# iOS Setup Guide

## Requirements

- iOS 14.0 or higher
- Xcode 13 or higher
- Swift 5.1 or higher

## Installation

```bash
npm install capacitor-edge-to-edge
npx cap sync ios
```

## Configuration

### 1. Info.plist Configuration

Add the following to your `Info.plist` to enable status bar appearance control:

```xml
<key>UIViewControllerBasedStatusBarAppearance</key>
<true/>
```

### 2. ViewController Configuration (Optional)

If you need more control over the status bar, you can extend your view controller:

```swift
import UIKit
import Capacitor

class ViewController: CAPBridgeViewController {
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        // This will be controlled by the plugin
        return super.preferredStatusBarStyle
    }
    
    override var prefersStatusBarHidden: Bool {
        return false
    }
}
```

## Usage Examples

### Basic iOS Setup

```typescript
import { EdgeToEdge } from 'capacitor-edge-to-edge';
import { Capacitor } from '@capacitor/core';

async function setupiOS() {
  if (Capacitor.getPlatform() === 'ios') {
    // Enable edge-to-edge
    await EdgeToEdge.enable();
    
    // Set status bar style
    await EdgeToEdge.setSystemBarAppearance({
      statusBarStyle: 'light' // Light icons for dark backgrounds
    });
    
    // Get safe area insets
    const insets = await EdgeToEdge.getSystemBarInsets();
    console.log('Status bar height:', insets.statusBar);
    console.log('Home indicator height:', insets.navigationBar);
    console.log('Safe area top:', insets.top);
    console.log('Safe area bottom:', insets.bottom);
  }
}
```

### React Hook for iOS

```typescript
import { useEffect, useState } from 'react';
import { EdgeToEdge } from 'capacitor-edge-to-edge';
import { Capacitor } from '@capacitor/core';

interface SafeAreaInsets {
  top: number;
  bottom: number;
  left: number;
  right: number;
}

function useIOSSafeArea() {
  const [insets, setInsets] = useState<SafeAreaInsets>({
    top: 0,
    bottom: 0,
    left: 0,
    right: 0
  });

  useEffect(() => {
    if (Capacitor.getPlatform() === 'ios') {
      const loadInsets = async () => {
        const result = await EdgeToEdge.getSystemBarInsets();
        setInsets({
          top: result.top,
          bottom: result.bottom,
          left: result.left,
          right: result.right
        });
      };
      
      loadInsets();
      
      // Update on orientation change
      window.addEventListener('resize', loadInsets);
      return () => window.removeEventListener('resize', loadInsets);
    }
  }, []);

  return insets;
}

// Usage in component
function MyComponent() {
  const insets = useIOSSafeArea();
  
  return (
    <div style={{
      paddingTop: `${insets.top}px`,
      paddingBottom: `${insets.bottom}px`,
      paddingLeft: `${insets.left}px`,
      paddingRight: `${insets.right}px`
    }}>
      {/* Your content */}
    </div>
  );
}
```

### Theme Switching on iOS

```typescript
async function setTheme(isDark: boolean) {
  if (Capacitor.getPlatform() === 'ios') {
    await EdgeToEdge.setSystemBarAppearance({
      statusBarStyle: isDark ? 'light' : 'dark'
    });
    
    // Optionally set background color
    await EdgeToEdge.setSystemBarColors({
      statusBarColor: isDark ? '#000000' : '#FFFFFF'
    });
  }
}
```

## iOS-Specific Notes

### Status Bar

- **Always Transparent**: iOS status bar is always transparent by design
- **Style Control**: Use `setSystemBarAppearance()` to switch between light and dark icons
- **Height Detection**: Use `getSystemBarInsets().statusBar` to get the height

### Safe Area Insets

iOS safe area includes:
- **Top**: Status bar + notch/Dynamic Island
- **Bottom**: Home indicator (34pt on most devices)
- **Left/Right**: Screen edges on landscape or edge-to-edge content

### Device-Specific Considerations

| Device Type | Top Inset | Bottom Inset |
|-------------|-----------|--------------|
| iPhone with notch | ~47-59pt | 34pt |
| iPhone with Dynamic Island | ~59pt | 34pt |
| iPhone SE / older | 20pt | 0pt |
| iPad | 20-24pt | 0-20pt |

### Orientation Changes

Safe area insets change during rotation. Listen to window resize events:

```typescript
window.addEventListener('resize', async () => {
  const insets = await EdgeToEdge.getSystemBarInsets();
  // Update your layout
});
```

## Troubleshooting

### Status bar style not changing

**Problem**: Status bar remains default style after calling `setSystemBarAppearance()`

**Solution**:
1. Check `Info.plist` has `UIViewControllerBasedStatusBarAppearance = true`
2. Ensure you're calling the method after view controller loads
3. Verify no other code is overriding `preferredStatusBarStyle`

### Incorrect safe area insets

**Problem**: `getSystemBarInsets()` returns zeros

**Solution**:
- Call the method after the view has fully loaded
- Check that the view controller is in the window hierarchy
- On first load, you may need a small delay:

```typescript
setTimeout(async () => {
  const insets = await EdgeToEdge.getSystemBarInsets();
  console.log(insets);
}, 100);
```

### Content still under status bar

**Problem**: Content appears behind status bar even after getting insets

**Solution**:
- Apply padding/margin using the inset values
- Use CSS safe area variables as fallback:

```css
.container {
  padding-top: env(safe-area-inset-top);
  padding-bottom: env(safe-area-inset-bottom);
}
```

## Best Practices

1. **Always check platform** before calling iOS-specific code
2. **Handle orientation changes** by re-fetching insets
3. **Combine with CSS** safe area variables for better compatibility
4. **Test on multiple devices** including notched and non-notched devices
5. **Consider landscape mode** where left/right insets may be non-zero

## Platform Differences

| Feature | iOS Behavior | Android Behavior |
|---------|-------------|------------------|
| Status bar color | Cannot change (always transparent) | Fully customizable |
| Navigation bar | Refers to home indicator | Refers to bottom nav buttons |
| Safe area | Includes notch/Dynamic Island | Includes system gesture areas |
| Transparency | Always transparent | Configurable |
