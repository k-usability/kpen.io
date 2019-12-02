import React, { useState, useEffect, useRef, Fragment } from "react";
import axios from "axios";
import Editor from '@monaco-editor/react';
import SplitPane from "react-split-pane";
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

const SubmittedProject = ({ match, history }) => {
    const edopt = {
        minimap: { enabled: false},
        selectOnLineNumbers: true,
        fontSize: 12,
        readOnly: true
    };

    console.log(process.env.REACT_APP_API_URL)

    const [project, setProject] = useState({programText: '', specText: '', jobs: [], stateType: null, state: 'Loading...', stateAlertType: 'warning'});
    const timers = useRef([]);

    useEffect(() => {
        function fetchProject() {
            axios
                .create({baseURL: process.env.REACT_APP_API_URL})
                .get('/api/project',
                { params: {"projectId": match.params.id} })
                .then(res => {
                    setProject(res.data);
                    console.log("set project: ");
                    console.log(res.data);

                    if (res.data.stateType === "Pending") {
                        console.log("setting timeout");
                        var tid = setTimeout(fetchProject, 3000);
                        console.log("Setting timeout value to tid: " + tid);
                        timers.current.push(tid)
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

        if (match.params.id) {
            fetchProject();
            return () => {
                console.log("calling cleanup")
                for (let i = 0; i < timers.current.length; i++) {
                    var tid = timers.current[i]
                    console.log("clearing timeout: ", tid)
                    clearTimeout(tid);
                }
            }
        }
    }, [match.params.id]);

    const handleNew = (_ev) => {
        history.push("/new")
    }

    const handleEdit = (_ev) => {
        history.push({
            pathname: "/new",
            state: {
                programText: project.programText,
                specText: project.specText
            }})
    }

    return (
        <div className="AppComponent">
            <div className="ActionButtons">
                <div className="button" onClick={handleNew}>
                    New
                </div>
                <div className="button" onClick={handleEdit}>
                    Edit
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
                                Program
                            </div>
                            <Editor language="sol" options={edopt} value={project.programText}/>
                        </div>
                        <div className="AppEditorComponent">
                            <div className="AppSectionHeader">
                                Specification
                            </div>
                            <Editor language="yaml" options={edopt} value={project.specText}/>
                        </div>
                    </SplitPane>
                    { project.jobs.length > 0 &&
                        <div className="AppResultsComponent">
                            <div className="AppSectionHeader">
                                Results
                            </div>
                            <Paper className="AppResultsPaper">
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Spec</TableCell>
                                            <TableCell>Submitted</TableCell>
                                            <TableCell>Result</TableCell>
                                            <TableCell>Output</TableCell>
                                            <TableCell>Error</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {
                                            project.jobs.map( (job, i) => (
                                                <TableRow key={i}>
                                                    <TableCell><a href={job.kUrl}>{job.specName}</a></TableCell>
                                                    <TableCell>{job.requestDt}</TableCell>
                                                    <TableCell className={job.resultStyle}>{job.result}</TableCell>
                                                    <TableCell><a href={job.outUrl}>stdout</a></TableCell>
                                                    <TableCell><a href={job.errUrl}>stderr</a></TableCell>
                                                </TableRow>
                                            ))
                                        }
                                    </TableBody>
                                </Table>
                            </Paper>
                        </div>
                            }
                </SplitPane>
            </div>
        </div>
    );
};

export default SubmittedProject;
