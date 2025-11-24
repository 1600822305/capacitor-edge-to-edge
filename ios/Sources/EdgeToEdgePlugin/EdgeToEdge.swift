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
    private var stageManagerOffset: CGFloat = 0
    
    // Keyboard event callbacks (official Capacitor Keyboard plugin style)
    var onKeyboardWillShow: ((CGFloat) -> Void)?
    var onKeyboardDidShow: ((CGFloat) -> Void)?
    var onKeyboardWillHide: (() -> Void)?
    var onKeyboardDidHide: (() -> Void)?
    
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
    /// Returns height in pixels (consistent with official Capacitor Keyboard plugin)
    @objc public func getKeyboardInfo() -> [String: Any] {
        return [
            "keyboardHeight": keyboardHeight,
            "isVisible": isKeyboardVisible
        ]
    }
    
    /// Show the keyboard
    /// Note: This is limited on iOS - it's better to focus an input element
    @objc public func showKeyboard() {
        // On iOS, we can't programmatically show keyboard without a focused input
        // This is a limitation of iOS platform
        // The official Capacitor Keyboard plugin marks this as unimplemented on iOS
    }
    
    /// Hide the keyboard
    @objc public func hideKeyboard() {
        DispatchQueue.main.async { [weak self] in
            self?.viewController?.view.endEditing(true)
        }
    }
    
    /// Setup keyboard notifications
    private func setupKeyboardNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardWillShow),
            name: UIResponder.keyboardWillShowNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardWillHide),
            name: UIResponder.keyboardWillHideNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardDidShow),
            name: UIResponder.keyboardDidShowNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardDidHide),
            name: UIResponder.keyboardDidHideNotification,
            object: nil
        )
    }
    
    /// Remove keyboard notifications
    private func removeKeyboardNotifications() {
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillHideNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardDidShowNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardDidHideNotification, object: nil)
    }
    
    /// Keyboard will show handler (official Capacitor Keyboard plugin approach)
    @objc private func keyboardWillShow(notification: NSNotification) {
        guard let keyboardFrame = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else {
            return
        }
        
        var height = keyboardFrame.size.height
        
        // Handle iPad Stage Manager (official Capacitor Keyboard plugin logic)
        if UIDevice.current.userInterfaceIdiom == .pad {
            if stageManagerOffset > 0 {
                height = stageManagerOffset
            } else {
                if let webView = viewController?.view,
                   let window = webView.window {
                    let screen = window.screen
                    let webViewAbsolute = webView.convert(webView.frame, to: screen.coordinateSpace)
                    height = (webViewAbsolute.size.height + webViewAbsolute.origin.y) - (screen.bounds.size.height - keyboardFrame.size.height)
                    if height < 0 {
                        height = 0
                    }
                    stageManagerOffset = height
                }
            }
        }
        
        keyboardHeight = height
        isKeyboardVisible = true
        
        // Notify callback (official Capacitor Keyboard plugin returns full height)
        onKeyboardWillShow?(height)
    }
    
    /// Keyboard did show handler
    @objc private func keyboardDidShow(notification: NSNotification) {
        guard let keyboardFrame = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else {
            return
        }
        
        let height = keyboardFrame.size.height
        
        // Notify callback
        onKeyboardDidShow?(height)
    }
    
    /// Keyboard will hide handler
    @objc private func keyboardWillHide(notification: NSNotification) {
        keyboardHeight = 0
        isKeyboardVisible = false
        
        // Notify callback
        onKeyboardWillHide?()
    }
    
    /// Keyboard did hide handler
    @objc private func keyboardDidHide(notification: NSNotification) {
        // Reset Stage Manager offset
        stageManagerOffset = 0
        
        // Notify callback
        onKeyboardDidHide?()
    }
}
