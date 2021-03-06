/*
 * This file is generated by jOOQ.
 */
package io.kpen.jooq.tables.records;


import io.kpen.jooq.tables.ProjectTag;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


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
public class ProjectTagRecord extends UpdatableRecordImpl<ProjectTagRecord> implements Record3<Integer, Integer, String> {

    private static final long serialVersionUID = 467903298;

    /**
     * Setter for <code>public.project_tag.id</code>.
     */
    public ProjectTagRecord setId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.project_tag.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.project_tag.project_id</code>.
     */
    public ProjectTagRecord setProjectId(Integer value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.project_tag.project_id</code>.
     */
    public Integer getProjectId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.project_tag.tag</code>.
     */
    public ProjectTagRecord setTag(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.project_tag.tag</code>.
     */
    public String getTag() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Integer, Integer, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Integer, Integer, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return ProjectTag.PROJECT_TAG.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return ProjectTag.PROJECT_TAG.PROJECT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return ProjectTag.PROJECT_TAG.TAG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component2() {
        return getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getTag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getTag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectTagRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectTagRecord value2(Integer value) {
        setProjectId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectTagRecord value3(String value) {
        setTag(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectTagRecord values(Integer value1, Integer value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ProjectTagRecord
     */
    public ProjectTagRecord() {
        super(ProjectTag.PROJECT_TAG);
    }

    /**
     * Create a detached, initialised ProjectTagRecord
     */
    public ProjectTagRecord(Integer id, Integer projectId, String tag) {
        super(ProjectTag.PROJECT_TAG);

        set(0, id);
        set(1, projectId);
        set(2, tag);
    }
}
