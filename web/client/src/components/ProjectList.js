import React, { useState, useEffect, Fragment } from "react";
import axios from "axios";
import {useAuth0} from "../react-auth0-spa";
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';


const ProjectList = ({ match, history }) => {
    console.log(process.env.REACT_APP_API_URL)

    const [projects, setProjects] = useState({jobs: []});
    const { user, loginWithPopup2 } = useAuth0();

    useEffect(() => {
        async function fetchProjects() {
            const token = await loginWithPopup2({"connection": "github"});
            if (token === null) {
                return;
            }

            axios
                .create({baseURL: process.env.REACT_APP_API_URL})
                .post('/api/projects',
                    { },
                    { headers: { 'Authorization': `Bearer ${token}` } })
                .then(res => setProjects(res.data))
                .catch(err => console.log(err));
        }

        fetchProjects();
    }, [user, loginWithPopup2]);

    return (
        <Paper>
        <Table>
            <TableHead>
            <TableRow>
                <TableCell>Job ID</TableCell>
                <TableCell>Spec</TableCell>
                <TableCell>Submitted</TableCell>
                <TableCell>Completed</TableCell>
                <TableCell>Result</TableCell>
                <TableCell>Output Log</TableCell>
                <TableCell>Error Log</TableCell>
                <TableCell>Project Url</TableCell>
            </TableRow>
            </TableHead>
            <TableBody>
                {
                    projects.jobs.map( (job, i) => (
                        <TableRow key={i}>
                            <TableCell scope="row">{job.id}</TableCell>
                            <TableCell>{job.specName}</TableCell>
                            <TableCell>{job.requestDt}</TableCell>
                            <TableCell>{job.completedDt}</TableCell>
                            <TableCell bgcolor={job.resultColor}>{job.result}</TableCell>
                            <TableCell><a href={job.outUrl}>stdout</a></TableCell>
                            <TableCell><a href={job.errUrl}>stderr</a></TableCell>
                            <TableCell><a href={job.projectUrl}>{job.projectUrl}</a></TableCell>
                        </TableRow>
                    ))
                }
            </TableBody>
        </Table>
        </Paper>
    );
};

export default ProjectList;
