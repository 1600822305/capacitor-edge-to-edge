# Keyboard Implementation Migration

## 🎯 Overview

We've migrated from our custom keyboard implementation to the **official Capacitor Keyboard plugin approach**. This provides better reliability, accuracy, and compatibility.

## ✨ What Changed

### Android Implementation

#### Before (Custom)
```java
// Used setOnApplyWindowInsetsListener
ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
    int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
    // Returned pixels
    listener.onKeyboardChanged(imeHeight, imeVisible);
});
```

#### After (Official Capacitor Approach)
```java
// Uses WindowInsetsAnimationCompat.Callback for precise animation tracking
ViewCompat.setWindowInsetsAnimationCallback(
    rootView,
    new WindowInsetsAnimationCompat.Callback(...) {
        @Override
        public WindowInsetsAnimationCompat.BoundsCompat onStart(...) {
            // Returns DP units (density-independent pixels)
            int imeHeightDp = Math.round(imeHeightPx / density);
            listener.onKeyboardWillShow(imeHeightDp);
        }
        
        @Override
        public void onEnd(...) {
            listener.onKeyboardDidShow(imeHeightDp);
        }
    }
);
```

**Key Improvements:**
- ✅ Uses animation callbacks (`onStart`/`onEnd`) instead of insets listener
- ✅ Distinguishes between `will` and `did` events
- ✅ Returns **DP units** instead of pixels (consistent across devices)
- ✅ More precise animation tracking

### iOS Implementation

#### Before (Custom)
```swift
// Subtracted safe area from keyboard height
let safeAreaBottom = viewController?.view.safeAreaInsets.bottom ?? 0
let actualHeight = keyboardFrame.height - safeAreaBottom
keyboardHeight = actualHeight
```

#### After (Official Capacitor Approach)
```swift
// Returns full keyboard height (no safe area subtraction)
var height = keyboardFrame.size.height

// Handle iPad Stage Manager (official logic)
if UIDevice.current.userInterfaceIdiom == .pad {
    if stageManagerOffset > 0 {
        height = stageManagerOffset
    } else {
        // Calculate Stage Manager offset
        let webViewAbsolute = webView.convert(webView.frame, to: screen.coordinateSpace)
        height = (webViewAbsolute.size.height + webViewAbsolute.origin.y) - 
                 (screen.bounds.size.height - keyboardFrame.size.height)
        stageManagerOffset = height
    }
}

keyboardHeight = height
```

**Key Improvements:**
- ✅ Returns **full keyboard height** (official behavior)
- ✅ iPad Stage Manager support
- ✅ Separate `will` and `did` event callbacks
- ✅ Compatible with official @capacitor/keyboard plugin

### API Changes

#### Event Data Format

**Before:**
```typescript
{
  height: number;           // Pixels (Android) or adjusted pixels (iOS)
  isVisible: boolean;
  animationDuration?: number; // iOS only
}
```

**After (Official Format):**
```typescript
{
  keyboardHeight: number;  // DP (Android) or pixels (iOS)
}
```

#### getKeyboardInfo() Response

**Before:**
```typescript
{
  height: number;
  isVisible: boolean;
}
```

**After (Official Format):**
```typescript
{
  keyboardHeight: number;  // DP (Android) or pixels (iOS)
  isVisible: boolean;
}
```

## 📋 Migration Guide

### 1. Update Event Listeners

**Before:**
```typescript
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  console.log('Height:', event.height);
  console.log('Visible:', event.isVisible);
  console.log('Duration:', event.animationDuration); // iOS only
});
```

**After:**
```typescript
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  console.log('Height:', event.keyboardHeight);
  // No isVisible in event - use separate will/did events
});
```

### 2. Update getKeyboardInfo() Calls

**Before:**
```typescript
const info = await EdgeToEdge.getKeyboardInfo();
console.log('Height:', info.height);
console.log('Visible:', info.isVisible);
```

**After:**
```typescript
const info = await EdgeToEdge.getKeyboardInfo();
console.log('Height:', info.keyboardHeight);  // Changed property name
console.log('Visible:', info.isVisible);      // Same
```

### 3. Unit Considerations

#### Android
- **Before:** Returned pixels
- **After:** Returns **DP units** (density-independent pixels)
- **Why:** Consistent across devices with different screen densities

**Example:**
```typescript
// If device has 3x density:
// Before: 900 pixels
// After: 300 DP

// To convert DP to pixels on Android (if needed):
const pixels = dpValue * window.devicePixelRatio;
```

#### iOS
- **Before:** Returned adjusted height (minus safe area)
- **After:** Returns **full keyboard height** (including safe area)
- **Why:** Official Capacitor Keyboard plugin behavior

**Example:**
```typescript
// iPhone with home indicator (safe area = 34):
// Before: 316 pixels (350 - 34)
// After: 350 pixels (full height)

// If you need the adjusted height:
const safeArea = await EdgeToEdge.getSystemBarInsets();
const adjustedHeight = keyboardHeight - safeArea.bottom;
```

## 🔄 Breaking Changes

### 1. Event Data Property Names
- `event.height` → `event.keyboardHeight`
- `event.isVisible` → **removed from event** (use `isKeyboardWillShow/Hide` to track state)
- `event.animationDuration` → **removed** (iOS specific, not in official plugin)

### 2. Units Changed
- **Android:** Pixels → DP units
- **iOS:** Adjusted height → Full height

### 3. Event Timing
- Now properly distinguishes between:
  - `keyboardWillShow` - Animation starts
  - `keyboardDidShow` - Animation completes
  - `keyboardWillHide` - Animation starts
  - `keyboardDidHide` - Animation completes

## ✅ Benefits

### 1. Better Accuracy
- **Android:** Animation callbacks provide precise timing
- **iOS:** Official handling of iPad Stage Manager

### 2. Consistency
- Matches official `@capacitor/keyboard` plugin API
- Easier migration if switching plugins

### 3. Compatibility
- Triggers window events for backward compatibility:
  ```javascript
  window.addEventListener('keyboardWillShow', (event) => {
    console.log(event.keyboardHeight);
  });
  ```

### 4. Cross-Platform Standards
- DP units on Android = consistent across devices
- Full keyboard height on iOS = standard behavior

## 📊 Comparison Table

| Feature | Before (Custom) | After (Official) |
|---------|----------------|------------------|
| **Android Units** | Pixels | DP (density-independent) |
| **iOS Height** | Adjusted (minus safe area) | Full height |
| **Event Timing** | Single change event | Separate will/did events |
| **Animation Tracking** | Insets listener | Animation callback |
| **iPad Support** | Basic | Stage Manager aware |
| **Event Data** | `height`, `isVisible`, `animationDuration` | `keyboardHeight` |
| **Compatibility** | Custom only | Compatible with @capacitor/keyboard |

## 🧪 Testing Checklist

- [ ] Test keyboard events on Android phones (various densities)
- [ ] Test keyboard events on Android tablets
- [ ] Test keyboard events on iPhone (with/without home indicator)
- [ ] Test keyboard events on iPad (with/without Stage Manager)
- [ ] Verify DP units on Android produce consistent layouts
- [ ] Verify full height on iOS works with your UI adjustments
- [ ] Test `getKeyboardInfo()` returns correct values
- [ ] Test event listener cleanup on component unmount

## 💡 Best Practices

### 1. Use DP-aware Layout on Android
```typescript
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  // event.keyboardHeight is already in DP
  // Use directly in your layout calculations
  const keyboardDp = event.keyboardHeight;
  
  // If you need pixels for some reason:
  const keyboardPx = keyboardDp * window.devicePixelRatio;
});
```

### 2. Handle Safe Area on iOS
```typescript
EdgeToEdge.addListener('keyboardWillShow', async (event) => {
  // Full keyboard height
  const fullHeight = event.keyboardHeight;
  
  // Get safe area if you need adjusted height
  const insets = await EdgeToEdge.getSystemBarInsets();
  const adjustedHeight = fullHeight - insets.bottom;
  
  // Use whichever makes sense for your UI
});
```

### 3. Use Separate Will/Did Events
```typescript
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  // Start animation immediately
  inputContainer.style.transition = 'transform 0.3s ease-out';
  inputContainer.style.transform = `translateY(-${event.keyboardHeight}px)`;
});

EdgeToEdge.addListener('keyboardDidShow', (event) => {
  // Keyboard animation complete - do final adjustments
  console.log('Keyboard fully visible');
});
```

## 🐛 Known Issues & Solutions

### Issue 1: Layout looks different on Android

**Cause:** DP units instead of pixels

**Solution:**
```typescript
// Don't multiply by devicePixelRatio - DP is what you want
// DP units automatically scale across devices
inputContainer.style.transform = `translateY(-${event.keyboardHeight}dp)`;
```

### Issue 2: iOS shows more keyboard height than before

**Cause:** Now returns full height (including safe area)

**Solution:**
```typescript
// Subtract safe area if needed
const insets = await EdgeToEdge.getSystemBarInsets();
const adjustedHeight = event.keyboardHeight - insets.bottom;
```

### Issue 3: Animation duration no longer available

**Cause:** Removed iOS-specific `animationDuration` for consistency

**Solution:**
```typescript
// Use a fixed duration (iOS keyboard animation is typically 0.3s)
const KEYBOARD_ANIMATION_DURATION = 300; // ms

inputContainer.style.transition = `transform ${KEYBOARD_ANIMATION_DURATION}ms ease-out`;
```

## 📚 References

- [Official Capacitor Keyboard Plugin](https://github.com/ionic-team/capacitor-plugins/tree/main/keyboard)
- [Android WindowInsetsAnimation](https://developer.android.com/reference/android/view/WindowInsetsAnimation)
- [iOS UIKeyboard Notifications](https://developer.apple.com/documentation/uikit/uikeyboard)

## 🎉 Summary

This migration brings your edge-to-edge plugin's keyboard handling in line with the official Capacitor Keyboard plugin, providing:
- ✅ Better accuracy and reliability
- ✅ Proper iPad Stage Manager support
- ✅ Consistent cross-platform behavior
- ✅ Industry-standard API

The changes are **breaking** but necessary for long-term maintainability and compatibility.
