#import <FBSDKCoreKit/FBSDKCoreKit.h>
#import <FBSDKLoginKit/FBSDKLoginKit.h>
#import <Cordova/CDV.h>

@interface FacebookConnect : CDVPlugin

- (void)isLoggedIn:(CDVInvokedUrlCommand*)command;
- (void)logInWithReadPermissions:(CDVInvokedUrlCommand*)command;
- (void)logInWithPublishPermissions:(CDVInvokedUrlCommand*)command;
- (void)getUserInfo:(CDVInvokedUrlCommand*)command;
- (void)logout:(CDVInvokedUrlCommand*)command;

@end
