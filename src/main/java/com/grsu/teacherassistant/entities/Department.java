package com.grsu.teacherassistant.entities;

import lombok.Getter;
import lombok.Setter;

import javax.faces.bean.ManagedBean;
import javax.persistence.*;
import java.util.Objects;

/**
 * @author Pavel Zaychick
 */
@Entity
@ManagedBean(name = "newInstanceOfDepartment")
@Getter
@Setter
public class Department implements AssistantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "abbreviation")
    private String abbreviation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Department that = (Department) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(abbreviation, that.abbreviation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (abbreviation != null ? abbreviation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Department{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", abbreviation='" + abbreviation + '\'' +
            '}';
    }
}
