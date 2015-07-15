#import "FacebookConnect.h"

@implementation FacebookConnect

- (void)isLoggedIn:(CDVInvokedUrlCommand*)command {
    BOOL isLoggedIn = [FBSDKAccessToken currentAccessToken] != nil;
    CDVPluginResult *pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsBool:isLoggedIn];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

CDVPluginResult* resultForLoginCallback(FBSDKLoginManagerLoginResult *result, NSError *error) {
    CDVPluginResult *pluginResult;
    if (error) {
        pluginResult = [CDVPluginResult
                        resultWithStatus:CDVCommandStatus_ERROR
                        messageAsDictionary:@{
                                              @"canceled": @false,
                                              @"message": [error localizedDescription]
                                              }];
    }
    else if (result.isCancelled) {
        pluginResult = [CDVPluginResult
                        resultWithStatus:CDVCommandStatus_ERROR
                        messageAsDictionary:@{
                                              @"canceled": @true
                                              }];
    }
    else {
        pluginResult = [CDVPluginResult
                        resultWithStatus:CDVCommandStatus_OK
                        messageAsDictionary:@{
                                              @"accessToken":[[result token] tokenString],
                                              @"recentlyGrantedPermissions":[[result grantedPermissions] allObjects],
                                              @"recentlyDeniedPermissions":[[result declinedPermissions] allObjects]
                                              }];
    }
    return pluginResult;
};

- (void)logInWithReadPermissions:(CDVInvokedUrlCommand*)command {
    FBSDKLoginManager *loginManager = [[FBSDKLoginManager alloc] init];
    NSArray *permissions = [command arguments];
    NSString *callbackId = [command callbackId];
    __weak CDVPlugin *weakSelf = self;
    [loginManager
     logInWithReadPermissions:permissions
     handler:^(FBSDKLoginManagerLoginResult *result, NSError *error) {
         CDVPluginResult *pluginResult = resultForLoginCallback(result, error);
         [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
     }];
}

- (void)logInWithPublishPermissions:(CDVInvokedUrlCommand*)command {
    FBSDKLoginManager *loginManager = [[FBSDKLoginManager alloc] init];
    NSArray *permissions = [command arguments];
    NSString *callbackId = [command callbackId];
    __weak CDVPlugin *weakSelf = self;
    [loginManager
     logInWithPublishPermissions:permissions
     handler:^(FBSDKLoginManagerLoginResult *result, NSError *error) {
         CDVPluginResult *pluginResult = resultForLoginCallback(result, error);
         [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
     }];
}

- (void)getUserInfo:(CDVInvokedUrlCommand*)command {
    if ([FBSDKAccessToken currentAccessToken]) {
        NSString *callbackId = [command callbackId];
        __weak CDVPlugin *weakSelf = self;
        [[[FBSDKGraphRequest alloc] initWithGraphPath:@"me" parameters:nil]
         startWithCompletionHandler:^(FBSDKGraphRequestConnection *connection, id result, NSError *error) {
             CDVPluginResult *pluginResult;
             if (error) {
                 pluginResult = [CDVPluginResult
                                 resultWithStatus:CDVCommandStatus_ERROR
                                 messageAsDictionary:@{
                                                       @"errorCode":@([error code]),
                                                       @"errorMessage":[error description],
                                                       @"errorRecoveryMessage":[error localizedRecoverySuggestion],
                                                       @"errorUserMessage":[error localizedDescription]
                                                       }];
             }
             else {
                 if ([result isKindOfClass:[NSDictionary class]]) {
                     pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsDictionary:result];
                 }
                 else if ([result isKindOfClass:[NSArray class]]) {
                     pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_OK
                                     messageAsArray:result];
                 }
                 else {
                     pluginResult = [CDVPluginResult
                                     resultWithStatus:CDVCommandStatus_JSON_EXCEPTION
                                     messageAsString:@"Could not read result :("];
                 }
             }
             [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
         }];
    }
}

- (void)logout:(CDVInvokedUrlCommand*)command {
    FBSDKLoginManager *loginManager = [[FBSDKLoginManager alloc] init];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [loginManager logOut];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[command callbackId]];
}

@end
