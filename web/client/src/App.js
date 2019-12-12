import React from "react";
import { Router, Route, Switch } from "react-router-dom";

import PrivateRoute from "./components/PrivateRoute";
import Loading from "./components/Loading";
import ProjectList from "./components/ProjectList";
import SubmittedProject from "./components/SubmittedProject";
import NewProject from "./components/NewProject";
import Documentation from "./components/Documentation";
import { useAuth0 } from "./react-auth0-spa";
import history from "./utils/history";
import { NavLink as RouterNavLink } from "react-router-dom";

// styles
import "./App.css";
import "./SplitPane.css";
// fontawesome
import initFontAwesome from "./utils/initFontAwesome";
initFontAwesome();

const App = () => {
  const { loading, isAuthenticated, logout } = useAuth0();

  if (loading) {
    return <Loading />;
  }

  const logoutWithRedirect = () =>
      logout({
        returnTo: window.location.origin
      });

  return (
    <Router history={history}>
      <div className="AppContainer">
        <div className="AppHeader">
          {/*<div className="AppLogo">
            <img src="" alt="logo"/>
          </div>*/}

          <RouterNavLink to="/new">
            New Project
          </RouterNavLink>
          <RouterNavLink to="/projects">
            Job Queue
          </RouterNavLink>

          <RouterNavLink to="/documentation">
            Documentation
          </RouterNavLink>

          {isAuthenticated &&
            <RouterNavLink className="push-left" to="#" onClick={() => logoutWithRedirect()}>
              Log out
            </RouterNavLink>
          }
        </div>

        <Switch>
          <Route path="/" exact render={(props) => <NewProject {...props} showExample={true}/>} />
          <Route path="/new" exact component={NewProject} />
          <Route path="/project/:id" exact component={SubmittedProject} />
          <Route path="/documentation" exact component={Documentation} />
          <PrivateRoute path="/projects" component={ProjectList} />
        </Switch>
      </div>
    </Router>
  );
};

export default App;
