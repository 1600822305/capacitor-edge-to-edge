# Capacitor Keyboard 风格实现

## 官方 @capacitor/keyboard 的实现方式

### **事件系统**

```typescript
// 4 个键盘事件
Keyboard.addListener('keyboardWillShow', (info: KeyboardInfo) => {
  console.log('keyboard will show with height:', info.keyboardHeight);
});

Keyboard.addListener('keyboardDidShow', (info: KeyboardInfo) => {
  console.log('keyboard did show with height:', info.keyboardHeight);
});

Keyboard.addListener('keyboardWillHide', () => {
  console.log('keyboard will hide');
});

Keyboard.addListener('keyboardDidHide', () => {
  console.log('keyboard did hide');
});
```

### **Android 特点**

根据官方文档：
> On Android keyboardWillShow and keyboardDidShow fire almost at the same time.
> On Android keyboardWillHide and keyboardDidHide fire almost at the same time.

### **实现要点**

1. **简单的监听器** - 只使用 `ViewCompat.setOnApplyWindowInsetsListener`
2. **同时触发 Will 和 Did** - Android 上两个事件几乎同时触发
3. **只报告高度变化** - 高度相同时不触发重复事件
4. **可靠性优先** - 不使用复杂的动画回调

## 我们应该采用的方案

```java
// ✅ 简单可靠的实现
public void setupKeyboardListener(KeyboardListener listener) {
    View decorView = activity.getWindow().getDecorView();
    
    final int[] lastHeight = {0};
    final boolean[] lastVisible = {false};
    
    ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
        Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
        boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
        int imeHeight = imeInsets.bottom;
        
        // 检测状态变化
        boolean heightChanged = imeHeight != lastHeight[0];
        boolean visibilityChanged = imeVisible != lastVisible[0];
        
        if (heightChanged || visibilityChanged) {
            lastHeight[0] = imeHeight;
            lastVisible[0] = imeVisible;
            
            if (listener != null) {
                // ✅ Capacitor 风格：同时触发 Will 和 Did 事件
                listener.onKeyboardChanged(imeHeight, imeVisible);
            }
        }
        
        return insets;
    });
}
```

## 为什么不用 WindowInsetsAnimation.Callback？

1. **官方 @capacitor/keyboard 不使用** - 他们选择简单可靠的方案
2. **Android 文档说明** - Will 和 Did 事件本来就几乎同时触发
3. **更少的 Bug** - 避免 DISPATCH_MODE 相关问题
4. **更好的兼容性** - 适用于所有 Android 版本

## 结论

✅ **采用 Capacitor 官方的简单方案**
❌ 不需要复杂的动画回调
❌ 不需要帧级别的更新
✅ 专注于可靠性，而不是"完美"的动画同步
