import React, { Component } from 'react'
import GoogleMapReact from 'google-map-react';

const AnyReactComponent = ({ text }) => <div>{text}</div>;

export default class GoogleMaps extends Component {
  static defaultProps = {
    center: {
      lat: 59.95,
      lng: 30.33
    },
    zoom: 11
  };

  render() {
    return (
      // Important! Always set the container height explicitly
      <div style={{ height: '100vh', width: '100%' }}>
        <GoogleMapReact
          bootstrapURLKeys={{ key: 'AIzaSyD8K9abyIciX6S97zIZ2fVn06kB-c-g3yE' }}
          defaultCenter={this.props.center}
          defaultZoom={this.props.zoom}
        >
          <AnyReactComponent
            lat={59.955413}
            lng={30.337844}
            text="My Marker"
          />
        </GoogleMapReact>

        {/* <iframe
          title="mapsIframe"
          width="600"
          height="450"
          frameborder="0" style={{border:0}}
          src="https://www.google.com/maps/embed/v1/place?key=AIzaSyD8K9abyIciX6S97zIZ2fVn06kB-c-g3yE
          &q=Space+Needle,Seattle+WA" allowfullscreen>
        </iframe> */}
      </div>
    );
  }
}
