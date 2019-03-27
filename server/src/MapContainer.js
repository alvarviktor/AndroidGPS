/*---------------------------------------------------------------------------------------
--	Source File:	MapContainer.js - Main file for the ReactJS app
--
--	Date:			March 25, 2019
--
--	Revisions:		(Date and Description)
--
--	Designer:		Daniel Shin, Evan Zhang
--
--	Programmer:		Evan Zhang
--
--	Notes:
--  This file is responsible for making the requests to the database and fetching the
--  data. Once the data is fetched, markers are drawn on the map and are positioned
--  depending on the latitude and longitude that was received. The user can click
--  on the markers for more detailed information about the data.
---------------------------------------------------------------------------------------*/

import React, {Component} from 'react';
import {Map, GoogleApiWrapper, Marker, InfoWindow} from 'google-maps-react';

const mapStyles = {
    width:  '100%',
    height: '100%'
};

export class MapContainer extends Component {
    constructor(props) {
        super(props);
        this.state = {
            coordinates:       [],
            seconds:           0,
            showingInfoWindow: false,
            activeMarker:      {},
        };
    };

    // Make the initial request to the databse
    componentDidMount = () => {
        fetch(`https://api.mlab.com/api/1/databases/comp4985/collections/coordinates?apiKey=Q3kcLrAb4liR46OZJ46LzwhScsXPYvLn`)
          .then(response => response.json())
          .then(response => this.setState({coordinates: response}))
          .then(() => this.interval = setInterval(() => this.tick(), 5000))
          .catch(err => console.log(err));
    };

    // Continous request to database every 5 seconds
    tick = () => {
        fetch(`https://api.mlab.com/api/1/databases/comp4985/collections/coordinates?apiKey=Q3kcLrAb4liR46OZJ46LzwhScsXPYvLn`)
          .then(response => response.json())
          .then(response => this.setState({coordinates: response}))
          .then(() => console.log(this.state.coordinates))
          .catch(err => console.log(err));

        this.setState(prevState => ({
            seconds: prevState.seconds + 1
        }));
    };

    componentWillUnmount() {
        clearInterval(this.interval);
    }

    // Render markers after fetching database
    renderMarkers = () => {
        return this.state.coordinates.map((e, i) => {
            return (
              <Marker
                key={i}
                onClick={this.onMarkerClick}
                name={e.device_id}
                device_ip={e.device_ip}
                time={e.time}
                lat= {e.lat}
                lng ={e.lng}
                position={{
                    lat: e.lat,
                    lng: e.lng
                }}/>
            );
        });
    };

    onMarkerClick = (props, marker, e) =>
      this.setState({
            selectedPlace:     props,
            activeMarker:      marker,
            showingInfoWindow: true
        });

    onClose = props => {
        if (this.state.showingInfoWindow) {
            this.setState({
                showingInfoWindow: false,
                activeMarker:      null
            });
        }
    };

    // Render the GoogleMap
    render() {
        return (
          <Map
            google={this.props.google}
            zoom={17}
            style={mapStyles}
            initialCenter={{
                lat: 49.248810,
                lng: -122.999835
            }}>
              {this.state.coordinates.length !== 0 ? this.renderMarkers() : ""}
              <InfoWindow
                marker={this.state.activeMarker}
                visible={this.state.showingInfoWindow}
                onClose={this.onClose}
              >
                  <div>
                      <h4>{this.state.activeMarker ? this.state.activeMarker.name : ''}</h4>
                      <h4>{this.state.activeMarker ? this.state.activeMarker.device_ip : ''}</h4>
                      <h4>{this.state.activeMarker ? this.state.activeMarker.time : ''}</h4>
                      <h4>{this.state.activeMarker ? "lat: " + this.state.activeMarker.lat : ''}</h4>
                      <h4>{this.state.activeMarker ? "long: " + this.state.activeMarker.lng : ''}</h4>

                  </div>
              </InfoWindow>
          </Map>
        );
    }
}

export default GoogleApiWrapper({
    apiKey: 'AIzaSyDbRmgeaLLdizP-YwTWIXbNanQixqdnqPU'
})(MapContainer);
