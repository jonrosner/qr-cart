import React, { Component } from 'react'
import ReactMapGL, { Marker, GeolocateControl, NavigationControl, Popup } from 'react-map-gl'
import Geocoder from 'react-map-gl-geocoder'
import Pin from './Pin'
import DeckGL, { GeoJsonLayer } from "deck.gl";
import SideBar from './SideBar'
import SideBarItem from './SideBarItem'

import 'mapbox-gl/dist/mapbox-gl.css'
import 'react-map-gl-geocoder/dist/mapbox-gl-geocoder.css';
import './styles/mapbox.css'
import StorePopup from './StorePopup';

const MAPBOX_TOKEN = 'pk.eyJ1IjoibHVnaXRhbiIsImEiOiJjazk0cnVvOGkwN3l4M25uMzNqMGh5eHVwIn0.ZAWvHiMklfu_4mmY1JhoSg';

const ICON = `M20.2,15.7L20.2,15.7c1.1-1.6,1.8-3.6,1.8-5.7c0-5.6-4.5-10-10-10S2,4.5,2,10c0,2,0.6,3.9,1.6,5.4c0,0.1,0.1,0.2,0.2,0.3
  c0,0,0.1,0.1,0.1,0.2c0.2,0.3,0.4,0.6,0.7,0.9c2.6,3.1,7.4,7.6,7.4,7.6s4.8-4.5,7.4-7.5c0.2-0.3,0.5-0.6,0.7-0.9
  C20.1,15.8,20.2,15.8,20.2,15.7z`;

const pinSize = 35;

export default class Mapbox extends Component {
    mapRef = React.createRef();
    geocoderContainerRef = React.createRef();
    pinRef = React.createRef();

    constructor(props) {
        super(props);
        this.state = {
            viewport: {
                latitude: 48.1340,
                longitude: 11.5247,
                zoom: 8,
                bearing: 0,
                pitch: 0
            },
            markerSettings: {
                display: 'none',
                size: 25,
            },
            marker: {
                latitude: 48.1340,
                longitude: 11.5247,
            },
            markerList: [
                {
                    latitude: 1.2,
                    longitude: 1.1,
                },
            ],
            events: {},
            selectedHotspot: null
        };
    }

    clickedHotspot(store) {
        this.setState({
            selectedHotspot: store
        })
    }

    _updateViewport = viewport => {
        this.setState({ viewport });
    };

    _logDragEvent(name, event) {
        this.setState({
            events: {
                ...this.state.events,
                [name]: event.lngLat
            }
        });
    }

    _onMarkerDragStart = event => {
        this._logDragEvent('onDragStart', event);
    };

    _onMarkerDrag = event => {
        this._logDragEvent('onDrag', event);
    };

    _onMarkerDragEnd = event => {
        this._logDragEvent('onDragEnd', event);
        this.setState({
            marker: {
                longitude: event.lngLat[0],
                latitude: event.lngLat[1]
            }
        });
    };

    dataFetch(that) {
        fetch('http://andromeda.goma-cms.org:1337/api/store/all').then(async res => {
            const data = await res.json();
            const markers = data.map(store => {
                return {
                    id: store.id,
                    name: store.name,
                    latitude: store.locationLat,
                    longitude: store.locationLng,
                    pplInStore: store.currentNoPeopleInStore,
                    address: store.address,
                    zip: store.zip,
                    city: store.city
                }
            })
            that.setState({
                ...that.state,
                markerList: markers
            })
        }).catch(error => {
            console.log("AN ERROR", error)
        })
    }

    componentDidMount() {
        setInterval(() => this.dataFetch(this), 1000);
        window.addEventListener("resize", this.resize);
        this.resize();
    }

    componentWillUnmount() {
        window.removeEventListener("resize", this.resize);
    }

    resize = () => {
        this.handleViewportChange({
            width: window.innerWidth,
            height: window.innerHeight
        });
    };

    closePopup = () => {
        this.setState({
          selectedHotspot: null
        }); 
    };

    handleViewportChange = viewport => {
        this.setState({
            viewport: { ...this.state.viewport, ...viewport }
        });
    };

    _onGeocoderViewportChange = viewport => {
        const geocoderDefaultOverrides = { transitionDuration: 1000 };

        return this.handleViewportChange({
            ...viewport,
            ...geocoderDefaultOverrides
        });
    };

    _handleOnResult = event => {
        this.setState({
            markerSettings: {
                display: 'block',
                size: 30,
            },
            marker: {
                latitude: event.result.geometry.coordinates[1],
                longitude: event.result.geometry.coordinates[0],
            },
            searchResultLayer: new GeoJsonLayer({
                id: "search-result",
                data: event.result.geometry,
                getFillColor: [255, 0, 0, 128],
                getRadius: 1000,
                pointRadiusMinPixels: 10,
                pointRadiusMaxPixels: 10
            })
        });
    };

    navStyle = {
        position: 'absolute',
        top: '40px',
        right: 0,
        padding: '10px'
    };

    geoStyle = {
        position: 'absolute',
        top: 0,
        right: 0,
        margin: '10px'
    };

    render() {
        const { viewport, searchResultLayer } = this.state;
        return (
            <div className="d-flex" id="wrapper">
                <SideBar>
                    <SideBarItem link='#' text="All Stores" />
                </SideBar>
                <div id="map-container">
                    <ReactMapGL
                        ref={this.mapRef}
                        {...viewport}
                        width="100%"
                        height="100vh"
                        mapStyle="mapbox://styles/mapbox/streets-v11"
                        onViewportChange={viewport => this.setState({ viewport })}
                        mapboxApiAccessToken={MAPBOX_TOKEN}
                    >
                        <Geocoder
                            position="top-left"
                            mapRef={this.mapRef}
                            containerRef={this.geocoderContainerRef}
                            onResult={this._handleOnResult}
                            onViewportChange={this._onGeocoderViewportChange}
                            mapboxApiAccessToken={MAPBOX_TOKEN}
                        />
                        <DeckGL {...viewport} layers={[searchResultLayer]} />

                        <GeolocateControl
                            style={this.geoStyle}
                            positionOptions={{ enableHighAccuracy: true }}
                            trackUserLocation={true}
                        />

                        <div className="nav" style={this.navStyle}>
                            <NavigationControl onViewportChange={this._updateViewport} />
                        </div>
                        {
                            this.state.markerList.map(el => {
                                return <Marker
                                    key={el.id}
                                    longitude={el.longitude}
                                    latitude={el.latitude}
                                    offsetTop={-pinSize}
                                    offsetLeft={-10}
                                    onHover={() => {
                                        console.log("KASKJDADSLK")
                                    }}

                                >
                                    <svg
                                        height={pinSize}
                                        viewBox="0 0 24 24"
                                        style={{
                                            cursor: 'pointer',
                                            fill: '#d00',
                                            stroke: 'none'
                                        }}
                                        onClick={() => this.clickedHotspot(el)}
                                    >
                                        <path d={ICON} />
                                    </svg>
                                </Marker>
                            })
                        }
                        {
                            this.state.selectedHotspot !== null ? (
                                <Popup
                                    tipSize={0}
                                    latitude={this.state.selectedHotspot.latitude}
                                    longitude={this.state.selectedHotspot.longitude}
                                    onClose={this.closePopup}
                                    offsetTop={-20}
                                    closeOnClick={false}
                                    captureClick={false}
                                >
                                    <StorePopup 
                                        id={this.state.selectedHotspot.id}
                                        name={this.state.selectedHotspot.name}
                                        address={this.state.selectedHotspot.address}
                                        pplInStore={this.state.selectedHotspot.pplInStore}
                                        zip={this.state.selectedHotspot.zip}
                                        city={this.state.selectedHotspot.city}
                                    />
                                </Popup>
                            ) : null
                        }
                        <Marker
                            longitude={this.state.marker.longitude}
                            latitude={this.state.marker.latitude}
                            offsetTop={-20}
                            offsetLeft={-10}
                            draggable
                            onDragStart={this._onMarkerDragStart}
                            onDrag={this._onMarkerDrag}
                            onDragEnd={this._onMarkerDragEnd}
                        >
                            <Pin size={this.state.markerSettings.size} show={this.state.markerSettings.display} />
                        </Marker>
                    </ReactMapGL>
                </div>
            </div>
        );
    }
}
