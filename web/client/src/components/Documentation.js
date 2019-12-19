import React, { useState, useEffect, Fragment } from "react";
import Vimeo from '@u-wave/react-vimeo';
import "../App.css"

const Documentation = ({ match, history }) => {
    console.log(process.env.REACT_APP_API_URL)



    return (<div className="documentation">
        <p>1. Watch the <a href="https://vimeo.com/379056987">tutorial video</a> for a quick walkthrough of this site.</p>
        <p>2. Read through <a href="https://kspec.io">kspec.io</a> to see examples of K-YAML specifications.</p>
        <p>3. Read the <a href="https://arxiv.org/abs/1912.02951">technical report</a>.</p>
        <p>4. Email kpen@consensys.net for support.</p>
        <br/>
        <br/>
        <br/>
        </div>
        );
};

export default Documentation;
