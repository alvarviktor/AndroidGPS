import React, {Component} from 'react';
import MapContainer from './MapContainer';
import LoginPage from './LoginPage';

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isAuthenticated: false
        };
    };

    onClickLogin = (id, pw) => {
        if (id == 'team4' && pw == 'team4isthebest')
            this.setState({isAuthenticated: true});
    };

    render() {
        return (
          <div className="App">
              {this.state.isAuthenticated ? <MapContainer/> : <LoginPage onClickLogin={this.onClickLogin}/>}
          </div>
        );
    }
}

export default App;
