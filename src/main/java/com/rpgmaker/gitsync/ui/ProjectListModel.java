package com.rpgmaker.gitsync.ui;

import com.rpgmaker.gitsync.model.Project;

import javax.swing.AbstractListModel;
import java.util.List;

/**
 * Simple JList Model f�r Projekte.
 */
public class ProjectListModel extends AbstractListModel<Project> {

    private final List<Project> projects;

    public ProjectListModel(List<Project> projects) {
        this.projects = projects;
    }

    @Override
    public int getSize() {
        return projects == null ? 0 : projects.size();
    }

    @Override
    public Project getElementAt(int index) {
        return projects.get(index);
    }

    public void addProject(Project project) {
        projects.add(project);
        int index = projects.size() - 1;
        fireIntervalAdded(this, index, index);
    }

    public void removeProject(Project project) {
        int index = projects.indexOf(project);
        if (index >= 0) {
            projects.remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

    public void refresh() {
        fireContentsChanged(this, 0, Math.max(0, projects.size() - 1));
    }
}
