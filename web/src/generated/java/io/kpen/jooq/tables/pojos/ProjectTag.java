/*
 * This file is generated by jOOQ.
 */
package io.kpen.jooq.tables.pojos;


import java.io.Serializable;

import javax.annotation.Generated;


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
public class ProjectTag implements Serializable {

    private static final long serialVersionUID = 1211640185;

    private final Integer id;
    private final Integer projectId;
    private final String  tag;

    public ProjectTag(ProjectTag value) {
        this.id = value.id;
        this.projectId = value.projectId;
        this.tag = value.tag;
    }

    public ProjectTag(
        Integer id,
        Integer projectId,
        String  tag
    ) {
        this.id = id;
        this.projectId = projectId;
        this.tag = tag;
    }

    public Integer getId() {
        return this.id;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public String getTag() {
        return this.tag;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProjectTag (");

        sb.append(id);
        sb.append(", ").append(projectId);
        sb.append(", ").append(tag);

        sb.append(")");
        return sb.toString();
    }
}