package com.matejdr.admanager.utils;

import com.matejdr.admanager.ProxyBannerAdView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PublisherAdViewCache {
    private  static Map<String, ProxyBannerAdView> cacheStore = new ConcurrentHashMap<>();


    public static ProxyBannerAdView getPublisherAdView(String key)  {
        return PublisherAdViewCache.cacheStore.get(key);
    }

    public static void setPublisherAdView(String key, ProxyBannerAdView adView) {
        PublisherAdViewCache.cacheStore.put(key, adView);
    }
}
