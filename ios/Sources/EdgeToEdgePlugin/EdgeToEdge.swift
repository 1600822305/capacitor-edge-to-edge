import Foundation
import UIKit

/// EdgeToEdge implementation for iOS 14+
/// Provides control over status bar appearance and safe area handling
@objc public class EdgeToEdge: NSObject {
    
    private weak var viewController: UIViewController?
    private var currentStatusBarStyle: UIStatusBarStyle = .default
    private var isEdgeToEdgeEnabled = false
    private var keyboardHeight: CGFloat = 0
    private var isKeyboardVisible = false
    private var keyboardListener: ((CGFloat, Bool, TimeInterval) -> Void)?
    
    // Keyboard mode tracking (like Flutter)
    private enum KeyboardMode {
        case hidden
        case docked
        case floating
    }
    private var currentKeyboardMode: KeyboardMode = .hidden
    
    @objc public init(viewController: UIViewController) {
        self.viewController = viewController
        super.init()
        setupKeyboardNotifications()
    }
    
    deinit {
        removeKeyboardNotifications()
    }
    
    /// Enable edge-to-edge mode (content extends to screen edges)
    /// On iOS, this is mostly handled by the app's configuration
    @objc public func enable() {
        isEdgeToEdgeEnabled = true
        
        guard let vc = viewController else { return }
        
        DispatchQueue.main.async {
            // Extend content under safe area
            vc.additionalSafeAreaInsets = .zero
            
            // Update view layout
            vc.view.setNeedsLayout()
            vc.view.layoutIfNeeded()
        }
    }
    
    /// Disable edge-to-edge mode (respect safe areas)
    @objc public func disable() {
        isEdgeToEdgeEnabled = false
        
        guard let vc = viewController else { return }
        
        DispatchQueue.main.async {
            // Restore default safe area behavior
            vc.additionalSafeAreaInsets = .zero
            
            vc.view.setNeedsLayout()
            vc.view.layoutIfNeeded()
        }
    }
    
    /// Set transparent system bars (iOS handles this automatically)
    /// Status bar is always transparent on iOS
    @objc public func setTransparentSystemBars(statusBar: Bool, navigationBar: Bool) {
        // iOS status bar is always transparent by default
        // This is a no-op but kept for API consistency
    }
    
    /// Set system bar colors
    /// On iOS, only background color can be set via the view controller
    @objc public func setSystemBarColors(statusBarColor: String?, navigationBarColor: String?) {
        guard let vc = viewController else { return }
        
        DispatchQueue.main.async {
            // iOS doesn't support changing status bar color directly
            // But we can change the view's background color
            if let colorHex = statusBarColor {
                if let color = self.hexToUIColor(hex: colorHex) {
                    vc.view.backgroundColor = color
                }
            }
        }
    }
    
    /// Set system bar appearance (light/dark icons)
    @objc public func setSystemBarAppearance(statusBarStyle: String?, navigationBarStyle: String?) {
        guard let style = statusBarStyle else { return }
        
        DispatchQueue.main.async {
            // Map "light" and "dark" to iOS status bar styles
            // "light" = light icons (for dark backgrounds) = .lightContent
            // "dark" = dark icons (for light backgrounds) = .darkContent
            if style == "light" {
                self.currentStatusBarStyle = .lightContent
            } else if style == "dark" {
                if #available(iOS 13.0, *) {
                    self.currentStatusBarStyle = .darkContent
                } else {
                    self.currentStatusBarStyle = .default
                }
            }
            
            // Trigger status bar update
            self.viewController?.setNeedsStatusBarAppearanceUpdate()
        }
    }
    
    /// Get current system bar insets (safe area insets on iOS)
    @objc public func getSystemBarInsets() -> [String: CGFloat] {
        guard let vc = viewController else {
            return [
                "statusBar": 0,
                "navigationBar": 0,
                "top": 0,
                "bottom": 0,
                "left": 0,
                "right": 0
            ]
        }
        
        var result: [String: CGFloat] = [:]
        
        if #available(iOS 11.0, *) {
            let safeArea = vc.view.safeAreaInsets
            let window = vc.view.window
            
            // Status bar height
            let statusBarHeight: CGFloat
            if #available(iOS 13.0, *) {
                statusBarHeight = window?.windowScene?.statusBarManager?.statusBarFrame.height ?? 0
            } else {
                statusBarHeight = UIApplication.shared.statusBarFrame.height
            }
            
            result = [
                "statusBar": statusBarHeight,
                "navigationBar": safeArea.bottom, // Home indicator height
                "top": safeArea.top,
                "bottom": safeArea.bottom,
                "left": safeArea.left,
                "right": safeArea.right
            ]
        } else {
            // Fallback for iOS < 11
            let statusBarHeight = UIApplication.shared.statusBarFrame.height
            result = [
                "statusBar": statusBarHeight,
                "navigationBar": 0,
                "top": statusBarHeight,
                "bottom": 0,
                "left": 0,
                "right": 0
            ]
        }
        
        return result
    }
    
    /// Get current status bar style for the view controller
    @objc public func getStatusBarStyle() -> UIStatusBarStyle {
        return currentStatusBarStyle
    }
    
    // MARK: - Helper Methods
    
    /// Convert hex color string to UIColor
    private func hexToUIColor(hex: String) -> UIColor? {
        var hexSanitized = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        hexSanitized = hexSanitized.replacingOccurrences(of: "#", with: "")
        
        var rgb: UInt64 = 0
        var alpha: CGFloat = 1.0
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        
        let length = hexSanitized.count
        
        guard Scanner(string: hexSanitized).scanHexInt64(&rgb) else { return nil }
        
        if length == 6 {
            // #RRGGBB
            red = CGFloat((rgb & 0xFF0000) >> 16) / 255.0
            green = CGFloat((rgb & 0x00FF00) >> 8) / 255.0
            blue = CGFloat(rgb & 0x0000FF) / 255.0
        } else if length == 8 {
            // #AARRGGBB
            alpha = CGFloat((rgb & 0xFF000000) >> 24) / 255.0
            red = CGFloat((rgb & 0x00FF0000) >> 16) / 255.0
            green = CGFloat((rgb & 0x0000FF00) >> 8) / 255.0
            blue = CGFloat(rgb & 0x000000FF) / 255.0
        } else {
            return nil
        }
        
        return UIColor(red: red, green: green, blue: blue, alpha: alpha)
    }
    
    // MARK: - Keyboard Methods
    
    /// Get current keyboard information
    @objc public func getKeyboardInfo() -> [String: Any] {
        return [
            "height": keyboardHeight,
            "isVisible": isKeyboardVisible
        ]
    }
    
    /// Set keyboard event listener
    @objc public func setKeyboardListener(_ listener: @escaping (CGFloat, Bool, TimeInterval) -> Void) {
        self.keyboardListener = listener
    }
    
    /// Setup keyboard notifications (Flutter-style: only 3 key notifications)
    private func setupKeyboardNotifications() {
        // keyboardWillShow: When docked keyboard appears or when keyboard goes from floating to docked
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleKeyboardNotification),
            name: UIResponder.keyboardWillShowNotification,
            object: nil
        )
        
        // keyboardWillChangeFrame: Immediately prior to any keyboard frame change
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleKeyboardNotification),
            name: UIResponder.keyboardWillChangeFrameNotification,
            object: nil
        )
        
        // keyboardWillHide: When keyboard is hidden or undocked
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleKeyboardNotification),
            name: UIResponder.keyboardWillHideNotification,
            object: nil
        )
    }
    
    /// Remove keyboard notifications
    private func removeKeyboardNotifications() {
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillChangeFrameNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    /// Main keyboard notification handler (Flutter-style unified handler)
    @objc private func handleKeyboardNotification(_ notification: NSNotification) {
        guard let userInfo = notification.userInfo,
              let keyboardFrameEnd = userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect,
              let duration = userInfo[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval else {
            return
        }
        
        // Determine keyboard mode
        let keyboardMode = calculateKeyboardMode(notification: notification, keyboardFrame: keyboardFrameEnd)
        
        // Calculate keyboard inset (only docked keyboards contribute to inset)
        let calculatedHeight = calculateKeyboardInset(keyboardFrame: keyboardFrameEnd, mode: keyboardMode)
        
        // Avoid duplicate triggers
        if self.keyboardHeight == calculatedHeight && self.currentKeyboardMode == keyboardMode {
            return
        }
        
        let wasVisible = isKeyboardVisible
        let previousHeight = keyboardHeight
        
        // Update state
        self.keyboardHeight = calculatedHeight
        self.currentKeyboardMode = keyboardMode
        self.isKeyboardVisible = (keyboardMode != .hidden && calculatedHeight > 0)
        
        // Notify listener
        if self.isKeyboardVisible && !wasVisible {
            // Keyboard showing
            keyboardListener?(calculatedHeight, true, duration)
        } else if !self.isKeyboardVisible && wasVisible {
            // Keyboard hiding
            keyboardListener?(0, false, duration)
        } else if self.isKeyboardVisible && previousHeight != calculatedHeight {
            // Keyboard height changed while visible
            keyboardListener?(calculatedHeight, true, duration)
        }
    }
    
    /// Calculate keyboard mode (hidden, docked, or floating)
    /// Based on Flutter's implementation
    private func calculateKeyboardMode(notification: NSNotification, keyboardFrame: CGRect) -> KeyboardMode {
        // If it's a hide notification, keyboard is hidden
        if notification.name == UIResponder.keyboardWillHideNotification {
            return .hidden
        }
        
        // If keyboard frame is zero, it's a floating shortcuts bar that was dragged and dropped
        if keyboardFrame.equalTo(.zero) {
            return .floating
        }
        
        // If keyboard frame is empty (width or height is 0), it's hidden
        if keyboardFrame.isEmpty {
            return .hidden
        }
        
        guard let view = viewController?.view,
              let window = view.window else {
            return .hidden
        }
        
        // Get screen bounds
        let screenBounds = window.screen.bounds
        
        // Calculate intersection between keyboard and screen
        let intersection = keyboardFrame.intersection(screenBounds)
        
        // If there's no meaningful intersection, keyboard is hidden
        if intersection.height <= 0 || intersection.width <= 0 {
            return .hidden
        }
        
        // If keyboard is above the bottom of screen, it's floating
        let screenHeight = screenBounds.height
        let keyboardBottom = keyboardFrame.maxY
        
        if round(keyboardBottom) < round(screenHeight) {
            return .floating
        }
        
        // Otherwise, it's docked
        return .docked
    }
    
    /// Calculate keyboard inset (Flutter-style: only docked keyboards count)
    private func calculateKeyboardInset(keyboardFrame: CGRect, mode: KeyboardMode) -> CGFloat {
        // Only docked keyboards contribute to inset
        guard mode == .docked else {
            return 0
        }
        
        guard let view = viewController?.view,
              let window = view.window else {
            return 0
        }
        
        // Calculate multitasking adjustment for Slide Over mode (iPad)
        let screenBounds = window.screen.bounds
        var adjustedKeyboardFrame = keyboardFrame
        let multitaskingAdjustment = calculateMultitaskingAdjustment(
            screenRect: screenBounds,
            keyboardFrame: keyboardFrame
        )
        adjustedKeyboardFrame.origin.y += multitaskingAdjustment
        
        // Convert view frame to screen coordinates
        let viewFrameInScreen = view.convert(view.bounds, to: nil)
        
        // Calculate intersection between adjusted keyboard and view
        let intersection = adjustedKeyboardFrame.intersection(viewFrameInScreen)
        
        // The portion of keyboard that's within the view
        let keyboardHeightInView = intersection.height
        
        // Return the height (this already accounts for safe area correctly)
        return keyboardHeightInView
    }
    
    /// Calculate multitasking adjustment for iPad Slide Over mode
    /// Based on Flutter's implementation
    private func calculateMultitaskingAdjustment(screenRect: CGRect, keyboardFrame: CGRect) -> CGFloat {
        guard let view = viewController?.view else {
            return 0
        }
        
        // Only apply to iPad in Slide Over mode
        // Slide Over characteristics: compact width + regular height on iPad
        if #available(iOS 8.0, *) {
            let traits = view.traitCollection
            
            guard traits.userInterfaceIdiom == .pad &&
                  traits.horizontalSizeClass == .compact &&
                  traits.verticalSizeClass == .regular else {
                return 0
            }
        } else {
            return 0
        }
        
        let screenHeight = screenRect.height
        let keyboardBottom = keyboardFrame.maxY
        
        // Skip if keyboard is already at screen bottom (Stage Manager mode)
        if screenHeight == keyboardBottom {
            return 0
        }
        
        // Calculate view's position relative to screen
        let viewRectInScreen = view.convert(view.bounds, to: nil)
        let viewBottom = viewRectInScreen.maxY
        
        // Calculate the space below the view
        let offset = screenHeight - viewBottom
        
        // Return offset if positive
        return offset > 0 ? offset : 0
    }
}
