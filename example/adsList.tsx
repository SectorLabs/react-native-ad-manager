/***
 Use this component inside your React Native Application.
 A scrollable list with different item type
 */
import React, { Component } from 'react';
import { View, Text, Dimensions, Button } from 'react-native';
import { RecyclerListView, DataProvider, LayoutProvider } from 'recyclerlistview';
import { Banner, BannerAdViewCacheManager } from 'react-native-ad-manager';

const ViewTypes = {
    FULL: 0,
    HALF_LEFT: 1,
    HALF_RIGHT: 2,
};

let containerCount = 0;

class CellContainer extends React.Component {
    constructor(args) {
        super(args);
        this._containerId = containerCount++;
    }
    render() {
        return (
            <View {...this.props}>
                {this.props.children}
                <Text>Cell Id: {this._containerId}</Text>
            </View>
        );
    }
}

const BannerWrapper = props => {
    React.useEffect(() => {
        console.log(`Mounted: ${props.slotIndex}`)
        return () => {
            console.log(`Unmounted: ${props.slotIndex}`)
        }
    }, []);

    React.useEffect(() => {
        console.log(`Rerender: ${props.slotIndex}`)
    }, [props.slotIndex])

    return (
        <Banner {...props} style={styles.fluid}/>
    )
};


/***
 * To test out just copy this component and render in you root component
 */
export default class AdsList extends React.Component {
    constructor(args) {
        super(args);

        let { width } = Dimensions.get('window');

        //Create the data provider and provide method which takes in two rows of data and return if those two are different or not.
        //THIS IS VERY IMPORTANT, FORGET PERFORMANCE IF THIS IS MESSED UP
        let dataProvider = new DataProvider((r1, r2) => {
            return r1 !== r2;
        });

        //Create the layout provider
        //First method: Given an index return the type of item e.g ListItemType1, ListItemType2 in case you have variety of items in your list/grid
        //Second: Given a type and object set the exact height and width for that type on given object, if you're using non deterministic rendering provide close estimates
        //If you need data based check you can access your data provider here
        //You'll need data in most cases, we don't provide it by default to enable things like data virtualization in the future
        //NOTE: For complex lists LayoutProvider will also be complex it would then make sense to move it to a different file
        this._layoutProvider = new LayoutProvider(
            index => {
                if (index % 4 === 0) {
                    return ViewTypes.FULL;
                } else if (index % 3 === 1) {
                    return ViewTypes.HALF_LEFT;
                } else {
                    return ViewTypes.HALF_RIGHT;
                }
            },
            (type, dim) => {
                switch (type) {
                    case ViewTypes.HALF_LEFT:
                        dim.width = width / 2;
                        dim.height = 300;
                        break;
                    case ViewTypes.HALF_RIGHT:
                        dim.width = width / 2;
                        dim.height = 300;
                        break;
                    case ViewTypes.FULL:
                        dim.width = width;
                        dim.height = 300;
                        break;
                    default:
                        dim.width = 0;
                        dim.height = 0;
                }
            },
        );

        this._rowRenderer = this._rowRenderer.bind(this);

        //Since component should always render once data has changed, make data provider part of the state
        this.state = {
            dataProvider: dataProvider.cloneWithRows(this._generateArray(300)),
        };
    }

    _generateArray(n) {
        let arr = new Array(n);
        for (let i = 0; i < n; i++) {
            arr[i] = i;
        }
        return arr;
    }

    //Given type and data return the view component
    _rowRenderer(type, data) {
        //You can return any view here, CellContainer has no special significance
        switch (type) {
            case ViewTypes.HALF_LEFT:
                return (
                    <CellContainer style={styles.containerGridLeft}>
                        <Text>Data: {data}</Text>
                    </CellContainer>
                );
            case ViewTypes.HALF_RIGHT:
                return (
                    <CellContainer style={styles.containerGridRight}>
                        <Text>Data: {data}</Text>
                    </CellContainer>
                );
            case ViewTypes.FULL:
                return (
                    <View style={{justifyContent: 'center', alignItems: 'center', width: '100%'}}>
                    <BannerWrapper
                        onSizeChange={values => console.log({ ...values, appEvent: 'sizeChanged' })}
                        onAdLoaded={ad => console.log({ ...ad, appEvent: 'addLoaded' })}
                        onAppEvent={sx => console.log({ ...sx, appEvent: 'appEvent' })}
                        style={{ width: Dimensions.get('screen').width, height: 'auto', flex: 1 }}
                        adSize={'fluid'}
                        validAdSizes={['300x250', 'banner', `fluid`]}
                        adUnitID={'/1000931/olx-pk_ios'}
                        testDevices={[Banner.simulatorId]}
                        targeting={{
                            customTargeting: { debug: true },
                        }}
                        slotIndex={`${data}`}
                    />
                    </View>
                );
            default:
                return null;
        }
    }

    render() {
        return (
            <View style={{width: '100%', height: '100%'}}>
                <Button onPress={BannerAdViewCacheManager.clear} title={'Clear cache'}/>
            <RecyclerListView
                layoutProvider={this._layoutProvider}
                dataProvider={this.state.dataProvider}
                rowRenderer={this._rowRenderer}
                forceNonDeterministicRendering
                canChangeSize
                style={{flex: 1, width: '100%'}}
            />
            </View>
        );
    }
}
const styles = {
    container: {
        justifyContent: 'space-around',
        alignItems: 'center',
        flex: 1,
        backgroundColor: '#00a1f1',
    },
    containerGridLeft: {
        justifyContent: 'space-around',
        alignItems: 'center',
        flex: 1,
        backgroundColor: '#ffbb00',
        height:300,
    },
    containerGridRight: {
        justifyContent: 'space-around',
        alignItems: 'center',
        flex: 1,
        backgroundColor: '#7cbb00',
        height: 300,
    },

    fluid: {
        width: Dimensions.get('screen').width,
        height: 300,
    }
};

export default AdsList;
