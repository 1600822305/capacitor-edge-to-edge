import Foundation
import Capacitor
import UIKit

/**
 * Capacitor Edge-to-Edge Plugin for iOS
 * Provides native control over status bar appearance and safe area handling
 * Supports iOS 14+
 */
@objc(EdgeToEdgePlugin)
public class EdgeToEdgePlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "EdgeToEdgePlugin"
    public let jsName = "EdgeToEdge"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "enable", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "disable", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setTransparentSystemBars", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setSystemBarColors", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setSystemBarAppearance", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getSystemBarInsets", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getKeyboardInfo", returnType: CAPPluginReturnPromise)
    ]
    
    private var implementation: EdgeToEdge?
    
    public override func load() {
        guard let viewController = self.bridge?.viewController else {
            return
        }
        implementation = EdgeToEdge(viewController: viewController)
        
        // Setup keyboard event listener
        implementation?.setKeyboardListener { [weak self] height, isVisible, duration in
            guard let self = self else { return }
            
            var event: [String: Any] = [
                "height": height,
                "isVisible": isVisible,
                "animationDuration": duration * 1000 // Convert to milliseconds
            ]
            
            // Send appropriate events
            if isVisible {
                self.notifyListeners("keyboardWillShow", data: event)
                // Send didShow after a short delay to match native behavior
                DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                    self.notifyListeners("keyboardDidShow", data: event)
                }
            } else {
                self.notifyListeners("keyboardWillHide", data: event)
                // Send didHide after animation completes
                DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                    self.notifyListeners("keyboardDidHide", data: event)
                }
            }
        }
    }
    
    /// Enable edge-to-edge mode
    @objc func enable(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        impl.enable()
        call.resolve()
    }
    
    /// Disable edge-to-edge mode
    @objc func disable(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        impl.disable()
        call.resolve()
    }
    
    /// Set system bars to transparent (no-op on iOS)
    @objc func setTransparentSystemBars(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        let statusBar = call.getBool("statusBar") ?? true
        let navigationBar = call.getBool("navigationBar") ?? true
        
        impl.setTransparentSystemBars(statusBar: statusBar, navigationBar: navigationBar)
        call.resolve()
    }
    
    /// Set system bar colors
    @objc func setSystemBarColors(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        let statusBarColor = call.getString("statusBarColor")
        let navigationBarColor = call.getString("navigationBarColor")
        
        impl.setSystemBarColors(statusBarColor: statusBarColor, navigationBarColor: navigationBarColor)
        call.resolve()
    }
    
    /// Set system bar appearance (light/dark icons)
    @objc func setSystemBarAppearance(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        let statusBarStyle = call.getString("statusBarStyle")
        let navigationBarStyle = call.getString("navigationBarStyle")
        
        impl.setSystemBarAppearance(statusBarStyle: statusBarStyle, navigationBarStyle: navigationBarStyle)
        call.resolve()
    }
    
    /// Get system bar insets
    @objc func getSystemBarInsets(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        let insets = impl.getSystemBarInsets()
        
        // Convert CGFloat to Double for JavaScript
        var result: [String: Double] = [:]
        for (key, value) in insets {
            result[key] = Double(value)
        }
        
        call.resolve(result)
    }
    
    /// Get keyboard information
    @objc func getKeyboardInfo(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        let info = impl.getKeyboardInfo()
        
        // Convert CGFloat to Double for JavaScript
        var result: [String: Any] = [:]
        for (key, value) in info {
            if let cgfloatValue = value as? CGFloat {
                result[key] = Double(cgfloatValue)
            } else {
                result[key] = value
            }
        }
        
        call.resolve(result)
    }
}
