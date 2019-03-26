import React, {Component} from 'react';

import {withStyles} from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import Button from '@material-ui/core/Button';

export class LoginPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            userId:   '',
            password: ''
        };
    };

    handleChange_name = userId => event => {
        this.setState({
            [userId]: event.target.value,
        });
    };

    handleChange_password = password => event => {
        this.setState({
            [password]: event.target.value,
        });
    };

    onClickLogin = () => {
        this.props.onClickLogin(this.state.userId, this.state.password);
    };

    render() {
        const {classes} = this.props;

        return (
          <div className={classes.root}>
              <AppBar position="static">
                  <Toolbar>
                      <IconButton className={classes.menuButton} color="inherit" aria-label="Menu">
                          <MenuIcon/>
                      </IconButton>
                      <Typography variant="h6" color="inherit" className={classes.grow}>
                          Welcome to Team 4 User Tracker
                      </Typography>
                  </Toolbar>
              </AppBar>
              <form className={classes.container} noValidate autoComplete="off">
                  <Grid container spacing={24}>
                      <Grid item xs={4}/>
                      <Grid item xs={4}>
                          <TextField
                            id="outlined-name"
                            label="User ID"
                            className={classes.textField}
                            value={this.state.userId}
                            onChange={this.handleChange_name('userId')}
                            margin="normal"
                            variant="outlined"
                          />
                      </Grid>
                      <Grid item xs={4}/>
                  </Grid>
                  <Grid container spacing={24}>
                      <Grid item xs={4}/>
                      <Grid item xs={4}>
                          <TextField
                            id="outlined-name"
                            label="Password"
                            className={classes.textField}
                            value={this.state.password}
                            onChange={this.handleChange_password('password')}
                            margin="normal"
                            variant="outlined"
                          />
                      </Grid>
                      <Grid item xs={4}/>
                  </Grid>
                  <Grid container spacing={24}>
                      <Grid item xs={4}/>
                      <Grid item xs={4}>
                          <Button variant="contained" color="secondary" className={classes.button}
                                  onClick={this.onClickLogin}>
                              Login
                          </Button>
                      </Grid>
                      <Grid item xs={4}/>
                  </Grid>
              </form>
          </div>
        );
    }
}

const styles = theme => ({
    container:  {
        display:  'flex',
        flexWrap: 'wrap',
    },
    textField:  {
        marginLeft:  0,
        marginRight: 0,
    },
    dense:      {
        marginTop: 16,
    },
    menu:       {
        width: 200,
    },
    root:       {
        flexGrow: 1,
    },
    menuButton: {
        marginLeft:  -12,
        marginRight: 20,
    },
    button:     {
        margin: theme.spacing.unit,
    },
});

LoginPage.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(LoginPage);