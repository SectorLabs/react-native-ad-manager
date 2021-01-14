import React, { Component } from 'react';
import {
    Button,
    Platform,
    ScrollView,
    StyleSheet,
    Text,
    View,
    RefreshControl,
    Dimensions,
} from 'react-native';
import { Interstitial, Banner, NativeAdsManager } from 'react-native-ad-manager';

const BannerExample = ({ style, title, children, ...props }) => (
    <View {...props} style={[styles.example, style]}>
        <View>{children}</View>
    </View>
);

export default class Example extends Component {
    constructor() {
        super();
        this.state = {
            fluidSizeIndex: 0,
            adsList: [],
            refreshingScrollView: false,
        };
    }

    render() {
        return (
            <View style={styles.container}>
                <Text style={{ marginBottom: 10 }}>DFP - Fluid Ad Size</Text>
                <View style={{ flex: 1, alignItems: 'center' }}>
                    <View
                        style={{
                            backgroundColor: 'red',
                            width: Dimensions.get('screen').width,
                            height: 20,
                        }}
                    />
                    <Banner
                        style={{ width: Dimensions.get('screen').width, height: 300 }}
                        onAdLoaded={ad => console.log({ ...ad, appEvent: 'addLoaded' })}
                        onAdFailedToLoad={error => console.log(error)}
                        onSizeChange={values => console.log({ ...values, appEvent: 'sizeChanged' })}
                        adSize={'fluid'}
                        validAdSizes={['300x250','banner', `fluid`]}
                        adUnitID={'/1000931/olx-pk_ios'}
                        onAppEvent={sx => console.log({ ...sx, appEvent: 'appEvent' })}
                        testDevices={[Banner.simulatorId]}
                        targeting={{
                            customTargeting: { debug: true },
                        }}
                    />
                    <View
                        style={{
                            backgroundColor: 'red',
                            width: Dimensions.get('screen').width,
                            height: 20,
                        }}
                    />
                </View>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        marginTop: Platform.OS === 'ios' ? 30 : 10,
    },
    example: {
        paddingVertical: 10,
    },
    title: {
        margin: 10,
        fontSize: 20,
    },
    button: {
        backgroundColor: '#CC5500',
    },
});
