package com.grsu.teacherassistant.entities;

import com.grsu.teacherassistant.converters.db.LocalDateTimeAttributeConverter;
import lombok.Getter;
import lombok.Setter;

import javax.faces.bean.ManagedBean;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author Pavel Zaychick
 */
@Entity
@ManagedBean(name = "newInstanceOfDiscipline")
@Getter
@Setter
public class Discipline implements AssistantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "create_date", insertable = false, updatable = false)
    private LocalDateTime createDate;

    @Basic
    @Column(name = "active")
    private Boolean active;

    @Basic
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL)
    private List<Stream> streams;

    public Discipline() {
    }

    public Discipline(Discipline discipline) {
        this.name = discipline.name;
        this.description = discipline.description;
        this.createDate = discipline.createDate;
        this.active = discipline.active;
        this.expirationDate = discipline.expirationDate;
        this.streams = discipline.streams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Discipline that = (Discipline) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(description, that.description)) return false;
        if (!Objects.equals(createDate, that.createDate)) return false;
        if (!Objects.equals(active, that.active)) return false;
        if (!Objects.equals(expirationDate, that.expirationDate))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (expirationDate != null ? expirationDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Discipline{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", createDate='" + createDate + '\'' +
            ", active=" + active +
            ", expirationDate='" + expirationDate + '\'' +
            '}';
    }
}
