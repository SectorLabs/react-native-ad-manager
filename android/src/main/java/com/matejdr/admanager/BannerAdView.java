package com.matejdr.admanager;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;

import com.matejdr.admanager.customClasses.CustomTargeting;
import com.matejdr.admanager.utils.PublisherAdViewCache;
import com.matejdr.admanager.utils.Targeting;

class BannerAdView extends ReactViewGroup implements AppEventListener, LifecycleEventListener {

    protected ProxyBannerAdView proxyBannerAdView;

    String[] testDevices;
    AdSize[] validAdSizes;
    String adUnitID;
    AdSize adSize;

    // Targeting
    Boolean hasTargeting = false;
    CustomTargeting[] customTargeting;
    String[] categoryExclusions;
    String[] keywords;
    String contentURL;
    String publisherProvidedID;
    Location location;
    String correlator;
    String slotIndex;

    private class MeasureAndLayoutRunnable implements Runnable {
        @Override
        public void run() {
            PublisherAdView adView = proxyBannerAdView.getAdView();
            boolean isFluid = proxyBannerAdView.isFluid();

            int top, left, width, height;

            if (isFluid) {
                top = 0;
                left = 0;
                width = getWidth();
                height = getHeight();
            } else {
                top = adView.getTop();
                left = adView.getLeft();
                width = adView.getAdSize().getWidthInPixels(getContext());
                height = adView.getAdSize().getHeightInPixels(getContext());
            }


                adView.measure(
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                );
                adView.layout(left, top, left + width, top + height);

        }
    }

    public BannerAdView(final Context context, ReactApplicationContext applicationContext) {
        super(context);
        applicationContext.addLifecycleEventListener(this);
        this.proxyBannerAdView = new ProxyBannerAdView();
        this.createAdView();
    }


    @Override
    public void requestLayout() {
        super.requestLayout();
        post(new MeasureAndLayoutRunnable());
    }

    public void onPublisherAdLoaded(){
        sendOnSizeChangeEvent();
    }

    private void createAdView() {
        // if (this.adView != null) this.adView.destroy();

        final Context context = getContext();
        PublisherAdView adView = proxyBannerAdView.createAdView(context);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                AdSize adSize = proxyBannerAdView.getAdView().getAdSize();
                boolean isFluid = proxyBannerAdView.isFluid();

                onAppEvent("AD_LOADED_STEP1", "");

                if (adSize == AdSize.SMART_BANNER || isFluid) {
                    proxyBannerAdView.setDimensions( (int) PixelUtil.toDIPFromPixel(getWidth()),
                            (int) PixelUtil.toDIPFromPixel(getHeight()));
                    onAppEvent("AD_LOADED_IS_FLUID", proxyBannerAdView.currentHeight.toString());
                } else {
                    proxyBannerAdView.setDimensions( adSize.getWidth(),  adSize.getHeight());
                    onAppEvent("AD_LOADED_IS_NOT_FLUID", proxyBannerAdView.currentHeight.toString());
                }

                onPublisherAdLoaded();

                WritableMap ad = Arguments.createMap();
                ad.putString("type", "banner");
                ad.putString("gadSize", adSize.toString());


                ad.putInt("measuredWidth", getMeasuredWidth());
                ad.putInt("measuredHeight", getMeasuredHeight());

                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_LOADED, ad);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                String errorMessage = "Unknown error";
                switch (errorCode) {
                    case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                        errorMessage = "Internal error, an invalid response was received from the ad server.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                        errorMessage = "Invalid ad request, possibly an incorrect ad unit ID was given.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                        errorMessage = "The ad request was unsuccessful due to network connectivity.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_NO_FILL:
                        errorMessage = "The ad request was successful, but no ad was returned due to lack of ad inventory.";
                        break;
                }
                WritableMap event = Arguments.createMap();
                WritableMap error = Arguments.createMap();
                error.putString("message", errorMessage);
                event.putMap("error", error);
                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_FAILED_TO_LOAD, event);
            }

            @Override
            public void onAdOpened() {
                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_OPENED, null);
            }

            @Override
            public void onAdClosed() {
                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_CLOSED, null);
            }

            @Override
            public void onAdLeftApplication() {
                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_LEFT_APPLICATION, null);
            }
        });

        this.addView(adView);
    }

    private void sendOnSizeChangeEvent() {
        WritableMap event = Arguments.createMap();

        event.putString("type", "banner");
        event.putDouble("width", proxyBannerAdView.currentWidth);
        event.putDouble("height", proxyBannerAdView.currentHeight);
        event.putString("adsize", adSize.toString());
        event.putBoolean("isFluid", proxyBannerAdView.isFluid());
        sendEvent(RNAdManagerBannerViewManager.EVENT_SIZE_CHANGE, event);
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                name,
                event);
    }

    public void loadBanner(String key) {
        proxyBannerAdView.loadBanner(
                adSize,
                validAdSizes,
                adUnitID,
                customTargeting,
                categoryExclusions,
                keywords,
                contentURL,
                publisherProvidedID,
                location,
                testDevices,
                correlator
        );

        PublisherAdViewCache.setPublisherAdView(key, this.proxyBannerAdView);
        onAppEvent("REQUEST_SENT", proxyBannerAdView.getAdView().getAdSize().toString());
    }

    public void reloadBanner(String key) {
        onAppEvent("RELOAD_BANNER", key);

        this.proxyBannerAdView = PublisherAdViewCache.getPublisherAdView(key);

        if (this.proxyBannerAdView == null) {
            this.proxyBannerAdView = new ProxyBannerAdView();
            this.createAdView();
            this.proxyBannerAdView.getAdView().setAdUnitId(this.adUnitID);
            this.loadBanner(key);
        }else {
            this.proxyBannerAdView.getAdView().resume();
            this.addView(this.proxyBannerAdView.getAdView());
            onPublisherAdLoaded();
            PublisherAdViewCache.setPublisherAdView(key, this.proxyBannerAdView);
        }
    }

    public void setAdUnitID(String adUnitID) {
        if (this.adUnitID != null) {
            // We can only set adUnitID once, so when it was previously set we have
            // to recreate the view
            this.createAdView();
        }
        this.adUnitID = adUnitID;
        proxyBannerAdView.getAdView().setAdUnitId(adUnitID);
    }

    public void setTestDevices(String[] testDevices) {
        this.testDevices = testDevices;
    }

    // Targeting
    public void setCustomTargeting(CustomTargeting[] customTargeting) {
        this.customTargeting = customTargeting;
    }

    public void setCategoryExclusions(String[] categoryExclusions) {
        this.categoryExclusions = categoryExclusions;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public void setContentURL(String contentURL) {
        this.contentURL = contentURL;
    }

    public void setPublisherProvidedID(String publisherProvidedID) {
        this.publisherProvidedID = publisherProvidedID;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }

    public void setValidAdSizes(AdSize[] adSizes) {
        this.validAdSizes = adSizes;
    }

    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    public void setSlotIndex(String slotIndex) {
        if (!slotIndex.equals(this.slotIndex) && this.slotIndex != null) {
            this.removeAllViews();
        }
        this.slotIndex = slotIndex;
    }

    @Override
    public void onAppEvent(String name, String info) {
        WritableMap event = Arguments.createMap();
        event.putString("name", name);
        event.putString("info", info);
        sendEvent(RNAdManagerBannerViewManager.EVENT_APP_EVENT, event);
    }

    @Override
    public void onHostResume() {
        proxyBannerAdView.resume();
    }

    @Override
    public void onHostPause() {
        proxyBannerAdView.pause();
    }

    @Override
    public void onHostDestroy() {
       proxyBannerAdView.destroy();
    }
}
