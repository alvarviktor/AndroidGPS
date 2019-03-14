import React, {Component} from 'react';
import {Map, GoogleApiWrapper, Marker} from 'google-maps-react';

const mapStyles = {
    width:  '100%',
    height: '100%'
};

export class MapContainer extends Component {
    constructor(props) {
        super(props);
        this.state = {
            coordinates: [],
            seconds:     0
        };
    };

    componentDidMount = () => {
        fetch(`https://api.mlab.com/api/1/databases/comp4985/collections/coordinates?apiKey=Q3kcLrAb4liR46OZJ46LzwhScsXPYvLn`)
          .then(response => response.json())
          .then(response => this.setState({coordinates: response}))
          .then(() => this.interval = setInterval(() => this.tick(), 5000))
          .catch(err => console.log(err));
    };

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

    renderMarkers = () => {
        return this.state.coordinates.map((e, i) => {
            return (
              <Marker key={i}
                      position={{
                          lat: e.lat,
                          lng: e.lng
                      }}/>
            );
        });
    };

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
          </Map>
        );
    }
}

export default GoogleApiWrapper({
    apiKey: 'AIzaSyDbRmgeaLLdizP-YwTWIXbNanQixqdnqPU'
})(MapContainer);
