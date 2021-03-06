/*
 * This file is generated by jOOQ.
 */
package io.kpen.jooq;


import io.kpen.jooq.tables.Job;
import io.kpen.jooq.tables.Person;
import io.kpen.jooq.tables.Project;
import io.kpen.jooq.tables.ProjectTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 93985995;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.job</code>.
     */
    public final Job JOB = io.kpen.jooq.tables.Job.JOB;

    /**
     * The table <code>public.person</code>.
     */
    public final Person PERSON = io.kpen.jooq.tables.Person.PERSON;

    /**
     * The table <code>public.project</code>.
     */
    public final Project PROJECT = io.kpen.jooq.tables.Project.PROJECT;

    /**
     * The table <code>public.project_tag</code>.
     */
    public final ProjectTag PROJECT_TAG = io.kpen.jooq.tables.ProjectTag.PROJECT_TAG;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        List result = new ArrayList();
        result.addAll(getSequences0());
        return result;
    }

    private final List<Sequence<?>> getSequences0() {
        return Arrays.<Sequence<?>>asList(
            Sequences.JOB_ID_SEQ,
            Sequences.PERSON_ID_SEQ,
            Sequences.PROJECT_ID_SEQ,
            Sequences.PROJECT_TAG_ID_SEQ);
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            Job.JOB,
            Person.PERSON,
            Project.PROJECT,
            ProjectTag.PROJECT_TAG);
    }
}
