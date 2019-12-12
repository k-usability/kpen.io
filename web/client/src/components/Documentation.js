import React, { useState, useEffect, Fragment } from "react";
import Vimeo from '@u-wave/react-vimeo';

const Documentation = ({ match, history }) => {
    console.log(process.env.REACT_APP_API_URL)


    return (<div><Vimeo video="256677962"/></div>);
};

export default Documentation;
