# Keyboard Handling Guide

This guide shows you how to handle the software keyboard with the edge-to-edge plugin.

## Features

- ✅ **Get Keyboard Height** - Query current keyboard height in pixels
- ✅ **Keyboard Visibility** - Check if keyboard is currently visible
- ✅ **Real-time Events** - Listen to keyboard show/hide events
- ✅ **Animation Duration** - Get keyboard animation timing (iOS)
- ✅ **Cross-platform** - Works on Android 11-16, iOS 14+, and Web

## Quick Start

### 1. Get Keyboard Information

```typescript
import { EdgeToEdge } from 'capacitor-edge-to-edge';

// Get current keyboard state
const keyboardInfo = await EdgeToEdge.getKeyboardInfo();

console.log('Keyboard height:', keyboardInfo.height);
console.log('Is visible:', keyboardInfo.isVisible);
```

### 2. Listen to Keyboard Events

```typescript
import { EdgeToEdge } from 'capacitor-edge-to-edge';

// Listen for keyboard show
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  console.log('Keyboard height:', event.height);
  console.log('Animation duration:', event.animationDuration); // iOS only
  
  // Adjust your UI
  const inputContainer = document.querySelector('.input-container');
  inputContainer.style.transform = `translateY(-${event.height}px)`;
});

// Listen for keyboard hide
EdgeToEdge.addListener('keyboardWillHide', (event) => {
  console.log('Keyboard hiding');
  
  // Reset your UI
  const inputContainer = document.querySelector('.input-container');
  inputContainer.style.transform = 'translateY(0)';
});

// Don't forget to clean up
EdgeToEdge.addListener('keyboardDidShow', (event) => {
  console.log('Keyboard animation completed');
});

EdgeToEdge.addListener('keyboardDidHide', (event) => {
  console.log('Keyboard hidden');
});
```

### 3. Remove Listeners

```typescript
// Remove all listeners when component unmounts
EdgeToEdge.removeAllListeners();
```

## React Example

### Basic Hook

```typescript
import { useEffect, useState } from 'react';
import { EdgeToEdge, KeyboardEvent } from 'capacitor-edge-to-edge';

function useKeyboard() {
  const [keyboardHeight, setKeyboardHeight] = useState(0);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const showListener = EdgeToEdge.addListener(
      'keyboardWillShow',
      (event: KeyboardEvent) => {
        setKeyboardHeight(event.height);
        setIsVisible(true);
      }
    );

    const hideListener = EdgeToEdge.addListener(
      'keyboardWillHide',
      (event: KeyboardEvent) => {
        setKeyboardHeight(0);
        setIsVisible(false);
      }
    );

    return () => {
      showListener.then(l => l.remove());
      hideListener.then(l => l.remove());
    };
  }, []);

  return { keyboardHeight, isVisible };
}

export default useKeyboard;
```

### Usage in Component

```typescript
import React from 'react';
import useKeyboard from './useKeyboard';

function ChatInput() {
  const { keyboardHeight, isVisible } = useKeyboard();

  return (
    <div
      className="input-container"
      style={{
        transform: `translateY(-${keyboardHeight}px)`,
        transition: 'transform 0.3s ease-out',
      }}
    >
      <input type="text" placeholder="Type a message..." />
      <button>Send</button>
    </div>
  );
}
```

## Vue 3 Example

```typescript
import { ref, onMounted, onUnmounted } from 'vue';
import { EdgeToEdge, type KeyboardEvent } from 'capacitor-edge-to-edge';

export function useKeyboard() {
  const keyboardHeight = ref(0);
  const isVisible = ref(false);

  let showListener: any;
  let hideListener: any;

  onMounted(async () => {
    showListener = await EdgeToEdge.addListener(
      'keyboardWillShow',
      (event: KeyboardEvent) => {
        keyboardHeight.value = event.height;
        isVisible.value = true;
      }
    );

    hideListener = await EdgeToEdge.addListener(
      'keyboardWillHide',
      (event: KeyboardEvent) => {
        keyboardHeight.value = 0;
        isVisible.value = false;
      }
    );
  });

  onUnmounted(() => {
    showListener?.remove();
    hideListener?.remove();
  });

  return { keyboardHeight, isVisible };
}
```

```vue
<template>
  <div
    class="input-container"
    :style="{
      transform: `translateY(-${keyboardHeight}px)`,
      transition: 'transform 0.3s ease-out'
    }"
  >
    <input type="text" placeholder="Type a message..." />
    <button>Send</button>
  </div>
</template>

<script setup lang="ts">
import { useKeyboard } from './composables/useKeyboard';

const { keyboardHeight, isVisible } = useKeyboard();
</script>
```

## Angular Example

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { EdgeToEdge, KeyboardEvent, PluginListenerHandle } from 'capacitor-edge-to-edge';

@Component({
  selector: 'app-chat-input',
  template: `
    <div
      class="input-container"
      [style.transform]="'translateY(-' + keyboardHeight + 'px)'"
      [style.transition]="'transform 0.3s ease-out'"
    >
      <input type="text" placeholder="Type a message..." />
      <button>Send</button>
    </div>
  `
})
export class ChatInputComponent implements OnInit, OnDestroy {
  keyboardHeight = 0;
  isVisible = false;
  
  private showListener?: PluginListenerHandle;
  private hideListener?: PluginListenerHandle;

  async ngOnInit() {
    this.showListener = await EdgeToEdge.addListener(
      'keyboardWillShow',
      (event: KeyboardEvent) => {
        this.keyboardHeight = event.height;
        this.isVisible = true;
      }
    );

    this.hideListener = await EdgeToEdge.addListener(
      'keyboardWillHide',
      (event: KeyboardEvent) => {
        this.keyboardHeight = 0;
        this.isVisible = false;
      }
    );
  }

  ngOnDestroy() {
    this.showListener?.remove();
    this.hideListener?.remove();
  }
}
```

## Platform-Specific Notes

### Android (11-16)

**How it works:**
- Uses `WindowInsetsCompat.Type.ime()` to detect IME (Input Method Editor)
- Works seamlessly with edge-to-edge mode
- Real-time height updates

**AndroidManifest.xml Configuration:**
```xml
<activity android:windowSoftInputMode="adjustResize">
```

**Best Practices:**
```typescript
// Android automatically handles keyboard
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  // Height is accurate on Android
  console.log('IME height:', event.height);
});
```

### iOS 14+

**How it works:**
- Uses `UIResponder.keyboardWillShowNotification`
- Automatically excludes safe area (home indicator)
- Provides animation duration

**Unique Features:**
```typescript
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  console.log('Keyboard height:', event.height); // Excludes home indicator
  console.log('Animation duration:', event.animationDuration); // In milliseconds
  
  // Use animation duration for smooth transitions
  const duration = event.animationDuration || 300;
  element.style.transition = `transform ${duration}ms ease-out`;
});
```

**iOS Considerations:**
- Height excludes safe area bottom (home indicator)
- Animation duration available for matching native animations
- Works with hardware and software keyboards

### Web/PWA

**How it works:**
- Uses Visual Viewport API
- Detects viewport height changes
- Heuristic-based keyboard detection

**Limitations:**
```typescript
// Web detection is approximate
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  // Height estimated based on viewport changes
  console.log('Estimated keyboard height:', event.height);
});
```

**Browser Support:**
- Chrome/Edge: ✅ Full support
- Safari: ✅ Full support (iOS 13+)
- Firefox: ⚠️ Limited (no Visual Viewport API)

## Common Use Cases

### 1. Fixed Input at Bottom

```typescript
import { EdgeToEdge } from 'capacitor-edge-to-edge';

// Keep input above keyboard
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  const inputBar = document.querySelector('.bottom-input');
  inputBar.style.bottom = `${event.height}px`;
});

EdgeToEdge.addListener('keyboardWillHide', () => {
  const inputBar = document.querySelector('.bottom-input');
  inputBar.style.bottom = '0px';
});
```

### 2. Scroll Input into View

```typescript
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  const activeInput = document.activeElement;
  if (activeInput && activeInput.tagName === 'INPUT') {
    setTimeout(() => {
      activeInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
  }
});
```

### 3. Adjust ScrollView

```typescript
EdgeToEdge.addListener('keyboardWillShow', (event) => {
  const scrollView = document.querySelector('.scroll-container');
  scrollView.style.paddingBottom = `${event.height}px`;
});

EdgeToEdge.addListener('keyboardWillHide', () => {
  const scrollView = document.querySelector('.scroll-container');
  scrollView.style.paddingBottom = '0px';
});
```

### 4. Chat App Pattern

```typescript
import { EdgeToEdge } from 'capacitor-edge-to-edge';

class ChatView {
  private messageList: HTMLElement;
  private inputContainer: HTMLElement;

  constructor() {
    this.messageList = document.querySelector('.messages');
    this.inputContainer = document.querySelector('.input-container');
    
    this.setupKeyboard();
  }

  setupKeyboard() {
    EdgeToEdge.addListener('keyboardWillShow', (event) => {
      // Move input up
      this.inputContainer.style.transform = `translateY(-${event.height}px)`;
      
      // Add padding to messages
      this.messageList.style.paddingBottom = `${event.height}px`;
      
      // Scroll to bottom
      setTimeout(() => {
        this.messageList.scrollTop = this.messageList.scrollHeight;
      }, 100);
    });

    EdgeToEdge.addListener('keyboardWillHide', () => {
      this.inputContainer.style.transform = 'translateY(0)';
      this.messageList.style.paddingBottom = '0';
    });
  }
}
```

## API Reference

### `getKeyboardInfo()`

```typescript
interface KeyboardInfo {
  height: number;      // Keyboard height in pixels
  isVisible: boolean;  // Whether keyboard is currently visible
}

const info = await EdgeToEdge.getKeyboardInfo();
```

### Event Listeners

```typescript
interface KeyboardEvent {
  height: number;            // Keyboard height in pixels
  isVisible: boolean;        // Visibility state
  animationDuration?: number; // Animation duration in ms (iOS only)
}

// Events:
- keyboardWillShow  // Fires before keyboard animation starts
- keyboardDidShow   // Fires after keyboard animation completes
- keyboardWillHide  // Fires before keyboard hides
- keyboardDidHide   // Fires after keyboard is hidden
```

## Troubleshooting

### Keyboard height is 0

**Solution:** Make sure you're calling after the keyboard has shown:
```typescript
EdgeToEdge.addListener('keyboardDidShow', async () => {
  const info = await EdgeToEdge.getKeyboardInfo();
  console.log(info.height); // Now has correct value
});
```

### Events not firing on Android

**Solution:** Set `windowSoftInputMode` in AndroidManifest.xml:
```xml
<activity android:windowSoftInputMode="adjustResize">
```

### iOS keyboard height seems wrong

**Reason:** iOS height automatically excludes safe area (home indicator).

**If you need total height:**
```typescript
const safeArea = await EdgeToEdge.getSystemBarInsets();
const totalHeight = keyboardHeight + safeArea.bottom;
```

### Web detection unreliable

**Solution:** Use Visual Viewport API check:
```typescript
if (window.visualViewport) {
  // Reliable detection
} else {
  // Fallback to CSS approach
}
```

## Performance Tips

1. **Debounce rapid events** (especially on Web)
2. **Use CSS transforms** instead of position changes
3. **Clean up listeners** when components unmount
4. **Match animation duration** on iOS for smooth UX

```typescript
// Good: Use transform
element.style.transform = `translateY(-${height}px)`;

// Avoid: Changing layout properties
element.style.bottom = `${height}px`; // Triggers layout
```

## Best Practices

1. ✅ Always remove listeners when component unmounts
2. ✅ Use `keyboardWillShow` for immediate response
3. ✅ Use `keyboardDidShow` for final adjustments
4. ✅ Test on real devices (simulators may differ)
5. ✅ Handle orientation changes
6. ✅ Consider hardware keyboard on tablets
7. ✅ Use CSS transitions for smooth animations
