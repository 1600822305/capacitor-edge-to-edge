#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Capacitor Plugin Registration Macros for EdgeToEdge iOS
// This file is required for Capacitor to discover and register the Swift plugin

CAP_PLUGIN(EdgeToEdgePlugin, "EdgeToEdge",
    CAP_PLUGIN_METHOD(enable, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(disable, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setTransparentSystemBars, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setSystemBarColors, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setSystemBarAppearance, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getSystemBarInsets, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getKeyboardInfo, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(show, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(hide, CAPPluginReturnPromise);
)
