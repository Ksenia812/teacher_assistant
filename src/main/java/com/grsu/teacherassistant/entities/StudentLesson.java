package com.grsu.teacherassistant.entities;

import com.grsu.teacherassistant.converters.db.LocalTimeAttributeConverter;
import com.grsu.teacherassistant.models.AdditionalLessonsInfo;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.List;

/**
 * @author Pavel Zaychick
 */
@NamedNativeQueries({
    @NamedNativeQuery(
        name = "StudentAdditionalLessons",
        query = "select count(*) from STUDENT_LESSON sl\n" +
            "join STUDENT s on sl.student_id = s.id\n" +
            "join STUDENT_GROUP sg on s.id = sg.student_id\n" +
            "join LESSON l on sl.lesson_id = l.id\n" +
            "where sl.student_id = :studentId\n" +
            "and sl.registered = 1\n" +
            "and l.group_id <> sg.group_id"),
    @NamedNativeQuery(
        name = "StudentAdditionalLessonsInfo",
        query = "select lt.name as lessonType, l.DATE as lessonDate\n" +
            "from STUDENT_LESSON sl\n" +
            "         join STUDENT s on sl.student_id = s.id\n" +
            "         join STUDENT_GROUP sg on s.id = sg.student_id\n" +
            "         join LESSON l on sl.lesson_id = l.id\n" +
            "        join LESSON_TYPE lt on l.type_id = lt.id\n" +
            "where sl.student_id =:studentId\n" +
            "  and sl.registered = 1\n" +
            "  and l.group_id <> sg.group_id"
    )
})
@Entity
@Table(name = "STUDENT_LESSON")
@Getter
@Setter
public class StudentLesson implements AssistantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "registered")
    private Boolean registered;

    @Basic
    @Convert(converter = LocalTimeAttributeConverter.class)
    @Column(name = "registration_time")
    private LocalTime registrationTime;

    @Basic
    @Column(name = "registration_type")
    private String registrationType;

    @Basic
    @Column(name = "mark")
    private String mark;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "lesson_id", referencedColumnName = "id")
    private Lesson lesson;

    @Basic
    @Column(name = "student_id", insertable = false, updatable = false)
    private Integer studentId;

    @Basic
    @Column(name = "lesson_id", insertable = false, updatable = false)
    private Integer lessonId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "entity_id", referencedColumnName = "id")
    @Where(clause = "type = 'STUDENT_LESSON'")
    private List<Note> notes;

    public List<Note> getNotes() {
        return notes;
    }

    public boolean isRegistered() {
        return Boolean.TRUE.equals(registered);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudentLesson that = (StudentLesson) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (registered != null ? !registered.equals(that.registered) : that.registered != null) return false;
        if (registrationTime != null ? !registrationTime.equals(that.registrationTime) : that.registrationTime != null)
            return false;
        if (registrationType != null ? !registrationType.equals(that.registrationType) : that.registrationType != null)
            return false;
        if (mark != null ? !mark.equals(that.mark) : that.mark != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (registered != null ? registered.hashCode() : 0);
        result = 31 * result + (registrationTime != null ? registrationTime.hashCode() : 0);
        result = 31 * result + (registrationType != null ? registrationType.hashCode() : 0);
        result = 31 * result + (mark != null ? mark.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StudentLesson{" +
            "id=" + id +
            ", registered=" + registered +
            ", registrationTime='" + registrationTime + '\'' +
            ", registrationType='" + registrationType + '\'' +
            ", mark='" + mark + '\'' +
            '}';
    }

}
