import Foundation
import Capacitor
import UIKit

/**
 * Capacitor Edge-to-Edge Plugin for iOS
 * Provides native control over status bar appearance and safe area handling
 * Supports iOS 14+
 */
@objc(EdgeToEdgePlugin)
public class EdgeToEdgePlugin: CAPPlugin {
    public let identifier = "EdgeToEdgePlugin"
    public let jsName = "EdgeToEdge"
    
    private var implementation: EdgeToEdge?
    
    public override func load() {
        guard let viewController = self.bridge?.viewController else {
            return
        }
        implementation = EdgeToEdge(viewController: viewController)
        
        // Setup keyboard event callbacks (official Capacitor Keyboard plugin approach)
        implementation?.onKeyboardWillShow = { [weak self] height in
            guard let self = self else { return }
            
            let data: [String: Any] = ["keyboardHeight": height]
            
            // Trigger window event (for compatibility with Capacitor Keyboard plugin)
            let jsData = "{ 'keyboardHeight': \(Int(height)) }"
            self.bridge?.triggerWindowJSEvent(eventName: "keyboardWillShow", data: jsData)
            
            // Notify listeners
            self.notifyListeners("keyboardWillShow", data: data)
        }
        
        implementation?.onKeyboardDidShow = { [weak self] height in
            guard let self = self else { return }
            
            let data: [String: Any] = ["keyboardHeight": height]
            
            // Trigger window event
            let jsData = "{ 'keyboardHeight': \(Int(height)) }"
            self.bridge?.triggerWindowJSEvent(eventName: "keyboardDidShow", data: jsData)
            
            // Notify listeners
            self.notifyListeners("keyboardDidShow", data: data)
        }
        
        implementation?.onKeyboardWillHide = { [weak self] in
            guard let self = self else { return }
            
            // Trigger window event
            self.bridge?.triggerWindowJSEvent(eventName: "keyboardWillHide")
            
            // Notify listeners
            self.notifyListeners("keyboardWillHide", data: nil)
        }
        
        implementation?.onKeyboardDidHide = { [weak self] in
            guard let self = self else { return }
            
            // Trigger window event
            self.bridge?.triggerWindowJSEvent(eventName: "keyboardDidHide")
            
            // Notify listeners
            self.notifyListeners("keyboardDidHide", data: nil)
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
    
    /// Show the keyboard
    @objc func show(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        impl.showKeyboard()
        call.unimplemented("Show keyboard is not supported on iOS")
    }
    
    /// Hide the keyboard
    @objc func hide(_ call: CAPPluginCall) {
        guard let impl = implementation else {
            call.reject("Plugin not initialized")
            return
        }
        
        impl.hideKeyboard()
        call.resolve()
    }
}
