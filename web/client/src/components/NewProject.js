import React, { useState, useEffect } from "react";
import axios from "axios";
import {useAuth0} from "../react-auth0-spa";
import SplitPane from "react-split-pane";
import Editor from "@monaco-editor/react";

var DEFAULT_PROGRAM_TEXT = "pragma solidity 0.5.0;\n" +
    "contract simple00 {\n" +
    "    function execute() public returns (uint) {\n" +
    "        return 5;\n" +
    "    }\n" +
    "}";

var DEFAULT_SPEC_TEXT = "spec:\n" +
    "  - rule:\n" +
    "      if:\n" +
    "        match:\n" +
    "          callData: \\#abiCallData(\"execute\", .TypedArgs)\n" +
    "      then:\n" +
    "        match:\n" +
    "          statusCode: EVMC_SUCCESS\n" +
    "          output: \\#encodeArgs(\\#uint256(5))"

const NewProject = ({ location, history, showExample }) => {
    const edopt = {
        minimap: { enabled: false},
        selectOnLineNumbers: true,
        fontSize: 12
    };

    console.log(process.env.REACT_APP_API_URL)

    var initialProgramText = '';
    var initialSpecText = '';
    if (showExample === true) {
        initialProgramText = DEFAULT_PROGRAM_TEXT
        initialSpecText = DEFAULT_SPEC_TEXT
    }

    const [project, setProject] = useState({programText: initialProgramText, specText: initialSpecText, state: null, stateAlertType: null});
    const { loginWithPopup2 } = useAuth0();

    useEffect(() => {
        setProject((project) => Object.assign({}, project, location.state));
    }, [location.state]);

    const handleSubmit = (event) => {
        async function fetchProve() {
            var token = await loginWithPopup2({connection: 'github'});
            if (token === null) {
                setProject((project) => Object.assign({}, project, {
                    state: "Authentication failed. Try refreshing the page or contact support@kpen.io.",
                    stateAlertType: "danger"
                }));
                return;
            }

            setProject((project) => Object.assign({}, project, {
                state: "Pre-processing",
                stateAlertType: "warning",
            }));

            axios
                .create({baseURL: process.env.REACT_APP_API_URL})
                .post('/api/project', {"programText": project.programText, "specText": project.specText},
                {  headers: { 'Authorization': `Bearer ${token}` } }
                )
                .then(res => {
                    if (res.data.projectId === null) {
                        setProject((project) => Object.assign({}, project, {
                            state: "Internal server error. Please contact support@kpen.io.",
                            stateAlertType: "danger",
                        }));
                    } else {
                        console.log("push: /project/" + res.data.projectId)
                        history.push("/project/" + res.data.projectId)
                    }
                })
                .catch(err => {
                    console.log(err)
                    setProject((project) => Object.assign({}, project, {
                        state: "Internal server error. Please contact support@kpen.io.",
                        stateAlertType: "danger",
                    }));
                });
        }

        if (event) event.preventDefault();
        fetchProve();
    }

    const programEditorDidMount = (_valueGetter, editor) => {
        editor.focus();
        editor.onDidChangeModelContent(ev => {
            setProject((project) => Object.assign({}, project, {programText: editor.getValue()}));
        });
    }

    const specEditorDidMount = (_valueGetter, editor) => {
        editor.onDidChangeModelContent(ev => {
            setProject((project) => Object.assign({}, project, {specText: editor.getValue()}));
        });
    }

    return (
        <div className="AppComponent">
            <div className="ActionButtons">
                <div className="button" onClick={handleSubmit}>
                    Prove
                </div>
                { project.state !== null &&
                <div className={"push-left alert " + project.stateAlertType}>
                    {project.state}
                </div>
                }
            </div>


            <div className="AppSplitPaneContainer">
                <SplitPane className="sp" defaultSize={600} split="vertical">
                    <SplitPane className="sp" defaultSize={300} split="horizontal">
                        <div className="AppEditorComponent">
                            <div className="AppSectionHeader">
                                Solidity Program
                            </div>
                            <Editor language="sol" options={edopt} value={project.programText} editorDidMount={programEditorDidMount} />
                        </div>
                        <div className="AppEditorComponent">
                            <div className="AppSectionHeader">
                                K-Yaml Specification
                            </div>
                        <Editor language="yaml" options={edopt} value={project.specText} editorDidMount={specEditorDidMount} />
                        </div>
                    </SplitPane>
                    <div>

                    </div>
                </SplitPane>
            </div>
        </div>
    );
};

export default NewProject;
