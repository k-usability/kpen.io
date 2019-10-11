/*
 * This file is generated by jOOQ.
 */
package io.kpen.jooq;


import io.kpen.jooq.tables.Job;
import io.kpen.jooq.tables.Person;
import io.kpen.jooq.tables.Project;
import io.kpen.jooq.tables.ProjectTag;

import javax.annotation.Generated;


/**
 * Convenience access to all tables in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>public.job</code>.
     */
    public static final Job JOB = io.kpen.jooq.tables.Job.JOB;

    /**
     * The table <code>public.person</code>.
     */
    public static final Person PERSON = io.kpen.jooq.tables.Person.PERSON;

    /**
     * The table <code>public.project</code>.
     */
    public static final Project PROJECT = io.kpen.jooq.tables.Project.PROJECT;

    /**
     * The table <code>public.project_tag</code>.
     */
    public static final ProjectTag PROJECT_TAG = io.kpen.jooq.tables.ProjectTag.PROJECT_TAG;
}