import React from 'react';
import { Switch, Route } from 'react-router-dom';

import NavBar from './components/Navbar'
import Mapbox from './components/Mapbox'
import Stores from './components/Stores'
import Store from './components/Store'
// import Reservations from './components/Reservation'

function App() {
  return (
    <div id="app-container">
      <NavBar />
        <main id="page-content-wrapper">
          {/* Insert Content of the Page here */}
          <Switch>
            <Route path='/' component={Mapbox} exact />
            <Route path='/stores' exact component={Stores} />
            <Route path='/stores/:id' component={Store} />
            {/* <Route path='/reservations' component={Reservations} /> */}
          </Switch>
        </main>
    </div>
  );
}

export default App;
