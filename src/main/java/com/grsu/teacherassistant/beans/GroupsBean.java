package com.grsu.teacherassistant.beans;

import com.grsu.teacherassistant.dao.EntityDAO;
import com.grsu.teacherassistant.dao.GroupDAO;
import com.grsu.teacherassistant.entities.Group;
import com.grsu.teacherassistant.entities.Student;
import lombok.Data;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Zaychick
 */
@ManagedBean(name = "groupsBean")
@ViewScoped
@Data
public class GroupsBean implements Serializable {

    private List<Group> groups;
    private List<Group> filteredGroups;
    private Group selectedGroup;

    private boolean showClosed;

    public void removeGroup(Group group) {
        EntityDAO.delete(group);
        groups.remove(group);
    }

    public List<Group> getGroups() {
        if (groups == null) {
            groups = GroupDAO.getAll(showClosed);
        }
        return groups;
    }

    public void search() {
        groups = null;
    }

    public void removeGroupCascade(Group group) {
        List<Student> all = EntityDAO.getAll(Student.class);
        List<Student> studentsToDelete = all.stream().filter(student -> student.getGroups().size() == 1 && student.getGroups().get(0).getId().equals(group.getId())).collect(Collectors.toList());
        EntityDAO.delete(studentsToDelete);
        EntityDAO.delete(group);
        groups.remove(group);
    }
}
