#import "CDVUpdateApp.h"
#import <Cordova/CDVPluginResult.h>

@implementation NSBundle (PluginExtensions)

+ (NSBundle*) pluginBundle:(CDVPlugin*)plugin {
    NSBundle* bundle = [NSBundle bundleWithPath: [[NSBundle mainBundle] pathForResource:NSStringFromClass([plugin class]) ofType: @"bundle"]];
    return bundle;
}
@end

#define PluginLocalizedString(plugin, key, comment) [[NSBundle pluginBundle:(plugin)] localizedStringForKey:(key) value:nil table:nil]



@implementation CDVUpdateApp

NSString *ipaPath;
NSString *_chineshWordPath;
NSString *_englishWordPath;
NSMutableDictionary *dict;

CDVUpdateApp *cmdUpdate;



- (void)getCurrentVersion:(CDVInvokedUrlCommand*)command
{
    //[self initUserLanguage];
    // NSString *URL = [command argumentAtIndex:0];
    NSMutableArray  *cmdArray  = [command argumentAtIndex:0];
    NSString *URL = [cmdArray objectAtIndex:0];
    NSString *curLanguage = [cmdArray objectAtIndex:1];
    
    
    [self initUserLanguage:curLanguage];

    [self.commandDelegate runInBackground:^{
        NSString* version = [self getCurrentVersionCode];
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:version];
        
        [self.commandDelegate evalJs:[pluginResult toSuccessCallbackString:command.callbackId]];
    }];
}

- (void)getServerVersion:(CDVInvokedUrlCommand*)command
{
    
    //[self initUserLanguage];

   // NSString *URL = [command argumentAtIndex:0];
    // NSString *URL = [command argumentAtIndex:0];
    NSMutableArray  *cmdArray  = [command argumentAtIndex:0];
    NSString *URL = [cmdArray objectAtIndex:0];
    NSString *curLanguage = [cmdArray objectAtIndex:1];
    
    
    [self initUserLanguage:curLanguage];
    
    [self.commandDelegate runInBackground:^{
        NSDictionary *resultDic = [self getServerVersionCode:URL];
        
        if (resultDic == nil) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"error"];
            
            [self.commandDelegate evalJs:[pluginResult toErrorCallbackString:command.callbackId]];
        } else {
            NSString *lastVersion = [resultDic objectForKey:@"verName"];
            
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:lastVersion];
            
            [self.commandDelegate evalJs:[pluginResult toSuccessCallbackString:command.callbackId]];
        }
    }];
}

- (void)checkAndUpdate:(CDVInvokedUrlCommand*)command
{
    
   // NSString *URL = [command argumentAtIndex:0];
    NSMutableArray  *cmdArray  = [command argumentAtIndex:0];
    NSString *URL = [cmdArray objectAtIndex:0];
    NSString *curLanguage = [cmdArray objectAtIndex:1];

    
    cmdUpdate = self;
    [self initUserLanguage:curLanguage];
    
    //NSBundle* bundle = [NSBundle bundleWithPath: [[NSBundle mainBundle] pathForResource:NSStringFromClass([CDVUpdateApp class]) ofType: @"bundle"]];

    
    NSString *curlanguage;
    
    if ([curlanguage isEqualToString:@"en-US"]) {
        curlanguage = @"en";
    }else
    {
        curlanguage = @"zh-Hans";
    }
    
    //NSBundle* bundle = [NSBundle bundleWithPath: [[NSBundle mainBundle] pathForResource:NSStringFromClass([plugin class]) ofType: @"bundle"]];
    //- (NSString *)pathForResource:(NSString *)name ofType:(NSString *)ext inDirectory:(NSString *)subpath forLocalization:(NSString *)localizationName;

    NSBundle *bundle = [NSBundle bundleWithPath: [[NSBundle mainBundle] pathForResource:NSStringFromClass([CDVUpdateApp class]) ofType: @"bundle"]];
    
    NSString *path = [bundle pathForResource:@"Localizable"
                                                     ofType:@"strings"
                                                inDirectory:nil
                                            forLocalization:curlanguage];
    
    
    //path = [[NSBundle mainBundle] pathForResource:@"Localizable" ofType:@"strings" inDirectory:@"" forLocalization:<#(NSString *)#>
    //
    //    // compiled .strings file becomes a "binary property list"
    dict = [NSMutableDictionary dictionaryWithContentsOfFile:path];
    NSLog(@"%@",[dict description]);
    
    NSString *titleInfo = PluginLocalizedString(cmdUpdate, @"titleInfo", nil);
    NSLog(@"titleInfo  >>>  %@",titleInfo);
    
    [self.commandDelegate runInBackground:^{
        NSDictionary *resultDic = [self getServerVersionCode:URL];
        
        if (resultDic) {
            NSString *lastVersion = [resultDic objectForKey:@"verName"];
            ipaPath = [resultDic objectForKey:@"ipaPath"];
            
            if ([lastVersion compare:[self getCurrentVersionCode] options:NSNumericSearch] == NSOrderedDescending) {
                
//                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"更新" message:NSLocalizedString()@"有新的版本，是否前往更新？" delegate:self cancelButtonTitle:@"关闭" otherButtonTitles:@"更新", nil];

                //UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"更新" message:NSLocalizedString(@"titleInfo","") delegate:self cancelButtonTitle:@"关闭" otherButtonTitles:@"更新", nil];
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"更新" message: [dict objectForKey:@"titleInfo"] delegate:self cancelButtonTitle:@"关闭" otherButtonTitles:@"更新", nil];
                
                alert.tag = 10000;
                dispatch_async(dispatch_get_main_queue(), ^{
                    [alert show];
                });
            }
        }
    }];
}


- (void)initUserLanguage:(NSString *) curlanguage{
    
//    NSUserDefaults *def = [NSUserDefaults standardUserDefaults];
//    NSString *string = [def valueForKey:@"userLanguage"];
//    if(string.length == 0){
//        //获取系统当前语言版本(中文zh-Hans,英文en)
//        NSArray* languages = [def objectForKey:@"AppleLanguages"];
//        NSString *current = [languages objectAtIndex:0];
//        string = current;
//        [def setValue:current forKey:@"userLanguage"];
//        [def synchronize];//持久化，不加的话不会保存
//    }
//    //获取文件路径
//    NSString *path = [[NSBundle mainBundle] pathForResource:string ofType:@"lproj"];
//    bundle = [NSBundle bundleWithPath:path];//生成bundle
    
    
    // 取得用户默认信息
    NSUserDefaults *defaults = [ NSUserDefaults standardUserDefaults ];
    // 取得 iPhone 支持的所有语言设置
    NSArray *languages = [defaults objectForKey : @"AppleLanguages" ];
    NSLog (@"%@", languages);
    
    // 获得当前iPhone使用的语言
    NSString* currentLanguage = [languages objectAtIndex:0];
    NSLog(@"当前使用的语言：%@",currentLanguage);
    
    _chineshWordPath = [[NSBundle mainBundle] pathForResource:@"Localizable" ofType:@"plist"];
    _englishWordPath =[[NSBundle mainBundle] pathForResource:@"Localizable" ofType:@"plist"];
    
    
    //    NSUserDefaults *def = [NSUserDefaults standardUserDefaults];
    //    NSString *language = [def valueForKey:@"userLanguage"];
    //    NSLog(@"当前使用的语言 >>> %@",language);
    
    NSString *loc = @"zh-Hans";
    //NSString *loc = @"en";
//        if ([curlanguage isEqualToString:@"zh-Hans"]) {
//            curlanguage = @"en";
//        }else
//        {
//            curlanguage = @"zh-Hans";
//        }
    
            if ([curlanguage isEqualToString:@"en-US"]) {
                curlanguage = @"en";
            }else
            {
                curlanguage = @"zh-Hans";
            }
    
    NSString *path = [[NSBundle mainBundle] pathForResource:@"Localizable"
                                                     ofType:@"strings"
                                                inDirectory:nil
                                            forLocalization:curlanguage];
    //
    //    // compiled .strings file becomes a "binary property list"
     dict = [NSMutableDictionary dictionaryWithContentsOfFile:path];
    
    //NSString *str = [dict objectForKey:_key];
    //    if (str) {
    //        return str;
    //    }
    //    return _key;
    
}

- (NSString *)userLanguage{
    
    NSUserDefaults *def = [NSUserDefaults standardUserDefaults];
    
    NSString *language = [def valueForKey:@"userLanguage"];
    
    return language;
}



- (NSString *)getCurrentVersionCode {
    return [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"];
}

- (NSDictionary *)getServerVersionCode:(NSString *)url {
    
    @try {
        NSError *error = nil;
        
        //加载一个NSURL对象
        NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:url] cachePolicy:NSURLRequestReloadIgnoringCacheData timeoutInterval:60.0f];
        //将请求的url数据放到NSData对象中
        NSData *response = [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:nil];
        //IOS5自带解析类NSJSONSerialization从response中解析出数据放到字典中
        NSDictionary *resultDic = [NSJSONSerialization JSONObjectWithData:response options:NSJSONReadingMutableLeaves error:&error];
        
        return resultDic;
    }
    @catch (NSException *exception) {
        return nil;
    }
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == 10000) {
        if (buttonIndex == 1) {
            NSURL *url = [NSURL URLWithString:ipaPath];
            [[UIApplication sharedApplication]openURL:url];
        }
    }
}


@end