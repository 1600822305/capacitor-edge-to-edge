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
        CAPPluginMethod(name: "getSystemBarInsets", returnType: CAPPluginReturnPromise)
    ]
    
    private var implementation: EdgeToEdge?
    
    public override func load() {
        guard let viewController = self.bridge?.viewController else {
            return
        }
        implementation = EdgeToEdge(viewController: viewController)
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
    
    // MARK: - Status Bar Override
    
    /// Override to allow dynamic status bar style changes
    public override var prefersStatusBarHidden: Bool {
        return false
    }
    
    public override var preferredStatusBarStyle: UIStatusBarStyle {
        return implementation?.getStatusBarStyle() ?? .default
    }
}
