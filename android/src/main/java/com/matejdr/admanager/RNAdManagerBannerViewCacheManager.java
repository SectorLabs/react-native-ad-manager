package com.matejdr.admanager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.module.annotations.ReactModule;
import com.matejdr.admanager.utils.PublisherAdViewCache;

import java.util.ArrayList;

@ReactModule(name = "CTKAdManagerBannerViewCacheManager")
public class RNAdManagerBannerViewCacheManager extends ReactContextBaseJavaModule {
    public static final String REACT_CLASS = "CTKAdManagerBannerViewCacheManager";

    public RNAdManagerBannerViewCacheManager(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void clearCache() {
        PublisherAdViewCache.clear();
    }
}
