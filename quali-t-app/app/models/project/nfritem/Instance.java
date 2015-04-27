package models.project.nfritem;

import models.AbstractEntity;
import models.project.Project;
import models.project.QualityProperty;
import models.template.CatalogQA;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by corina on 03.04.2015.
 */

@Entity
@Table(name = "instance")
@Nullable
public class Instance extends AbstractEntity {

    public Instance() {
    }

    public Instance(String description, CatalogQA qa, List<QualityProperty> qps) {
        this.description = description;
        this.template = qa;
        for (QualityProperty qp : qps){
            qualityPropertyStatus.add(new QualityPropertyStatus(this, qp));
        }
    }
    private String description;

    @ManyToOne
    private Project project;

    @ManyToOne(optional = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private CatalogQA template;

    @OneToMany(mappedBy = "instance")
    private Set<Val> values = new HashSet<>();

    @OneToMany(mappedBy = "qa", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<QualityPropertyStatus> qualityPropertyStatus = new HashSet<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public CatalogQA getTemplate() {
        return template;
    }

    public void setTemplate(CatalogQA template) {
        this.template = template;
    }

    public Set<Val> getValues() {
        return values;
    }

    public void setValues(Set<Val> values) {
        this.values = values;
    }
}
