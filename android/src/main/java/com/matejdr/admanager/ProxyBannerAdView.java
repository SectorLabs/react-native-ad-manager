package com.matejdr.admanager;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.facebook.react.views.view.ReactViewGroup;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.matejdr.admanager.customClasses.CustomTargeting;
import com.matejdr.admanager.utils.Targeting;

import java.util.ArrayList;
import java.util.Arrays;

public class ProxyBannerAdView {
    private PublisherAdView adView;
    public Integer currentWidth;
    public Integer currentHeight;

    public PublisherAdView getAdView() {
        return adView;
    }

    public boolean isFluid() {
        AdSize adSize = adView.getAdSize();

        if (adSize == null) {
            return false;
        }

        return adSize == AdSize.FLUID || adSize.toString().equals("320x50_mb");
    }

    public PublisherAdView createAdView(Context context) {
        PublisherAdView.LayoutParams layoutParams = new PublisherAdView.LayoutParams(ReactViewGroup.LayoutParams.MATCH_PARENT, ReactViewGroup.LayoutParams.WRAP_CONTENT);

        PublisherAdView adView = new PublisherAdView(context);
        adView.setLayoutParams(layoutParams);

        this.adView = adView;


        return this.adView;
    }

    public PublisherAdRequest buildRequest(String adUnitID, CustomTargeting[] customTargeting, String[] categoryExclusions, String[] keywords, String contentURL, String publisherProvidedID, Location location, String[] testDevices, String correlator) {
        PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

        if (testDevices != null) {
            for (String device : testDevices) {
                String testDevice = device;
                if (testDevice == "SIMULATOR") {
                    testDevice = PublisherAdRequest.DEVICE_ID_EMULATOR;
                }
                adRequestBuilder.addTestDevice(testDevice);
            }
        }

        if (correlator == null) {
            correlator = (String) Targeting.getCorelator(adUnitID);
        }
        Bundle bundle = new Bundle();
        bundle.putString("correlator", correlator);

        adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);

        // Targeting
        if (customTargeting != null && customTargeting.length > 0) {
            for (int i = 0; i < customTargeting.length; i++) {
                String key = customTargeting[i].key;
                if (!key.isEmpty()) {
                    if (customTargeting[i].value != null && !customTargeting[i].value.isEmpty()) {
                        adRequestBuilder.addCustomTargeting(key, customTargeting[i].value);
                    } else if (customTargeting[i].values != null && !customTargeting[i].values.isEmpty()) {
                        adRequestBuilder.addCustomTargeting(key, customTargeting[i].values);
                    }
                }
            }
        }
        if (categoryExclusions != null && categoryExclusions.length > 0) {
            for (int i = 0; i < categoryExclusions.length; i++) {
                String categoryExclusion = categoryExclusions[i];
                if (!categoryExclusion.isEmpty()) {
                    adRequestBuilder.addCategoryExclusion(categoryExclusion);
                }
            }
        }
        if (keywords != null && keywords.length > 0) {
            for (int i = 0; i < keywords.length; i++) {
                String keyword = keywords[i];
                if (!keyword.isEmpty()) {
                    adRequestBuilder.addKeyword(keyword);
                }
            }
        }
        if (contentURL != null) {
            adRequestBuilder.setContentUrl(contentURL);
        }
        if (publisherProvidedID != null) {
            adRequestBuilder.setPublisherProvidedId(publisherProvidedID);
        }
        if (location != null) {
            adRequestBuilder.setLocation(location);
        }

        return adRequestBuilder.build();
    }

    public void loadBanner(AdSize adSize, AdSize[] validAdSizes, String adUnitID, CustomTargeting[] customTargeting, String[] categoryExclusions, String[] keywords, String contentURL, String publisherProvidedID, Location location, String[] testDevices, String correlator) {
        ArrayList<AdSize> adSizes = new ArrayList<AdSize>();
        if (adSize != null) {
            adSizes.add(adSize);
        }

        if (validAdSizes != null) {
            adSizes.addAll(Arrays.asList(validAdSizes));
        }

        if (adSizes.size() == 0) {
            adSizes.add(AdSize.BANNER);
        }

        this.adView.setAdSizes(adSizes.toArray(new AdSize[adSizes.size()]));

        this.adView.loadAd(this.buildRequest(adUnitID, customTargeting, categoryExclusions,  keywords, contentURL,publisherProvidedID, location, testDevices, correlator));

    }

    public void resume(){
        if (this.adView != null) {
            this.adView.resume();
        }
    }

    public void pause() {
        if (this.adView != null) {
            this.adView.pause();
        }
    }

    public void destroy() {
        if (this.adView != null) {
            this.adView.destroy();
        }
    }

    public void setDimensions(Integer width, Integer height) {
        this.currentHeight = height;
        this.currentWidth = width;
    }

}
