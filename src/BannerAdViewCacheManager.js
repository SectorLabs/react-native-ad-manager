import { NativeModules, NativeEventEmitter } from 'react-native';

const CTKAdManagerBannerViewCacheManager = NativeModules.CTKAdManagerBannerViewCacheManager;

export default class BannerViewCacheManager {
  static clear(){
        CTKAdManagerBannerViewCacheManager.clearCache()
  }
}
