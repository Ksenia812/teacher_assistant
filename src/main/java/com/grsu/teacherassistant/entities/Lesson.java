package com.grsu.teacherassistant.entities;

import com.grsu.teacherassistant.converters.db.LessonTypeAttributeConverter;
import com.grsu.teacherassistant.converters.db.LocalDateTimeAttributeConverter;
import com.grsu.teacherassistant.models.LessonType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.faces.bean.ManagedBean;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Zaychick
 */
@Entity
@ManagedBean(name = "newInstanceOfLesson")
@Getter
@Setter
public class Lesson implements AssistantEntity {
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
    @Column(name = "checked")
    private Boolean checked;

    @Basic
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "create_date", insertable = false, updatable = false)
    private LocalDateTime createDate;

    @Basic
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "date")
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "stream_id", referencedColumnName = "id")
    private Stream stream;

    @Convert(converter = LessonTypeAttributeConverter.class)
    @Column(name = "type_id")
    private LessonType type;

    @Column(name = "index_number")
    private Integer index;

    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "entity_id", referencedColumnName = "id")
    @Where(clause = "type = 'LESSON'")
    private List<Note> notes;

    @ManyToOne
    @JoinColumn(name = "schedule_id", referencedColumnName = "id")
    private Schedule schedule;

    @MapKey(name = "studentId")
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private Map<Integer, StudentLesson> studentLessons;

    @Transient
    private Lesson lastLectureLesson;
    @Transient
    private Lesson lastPracticeLesson;

    public Lesson() {
        this.checked = false;
    }

    public Lesson(Lesson lesson) {
        this.id = lesson.id;
        this.name = lesson.name;
        this.description = lesson.description;
        this.createDate = lesson.createDate;
        this.date = lesson.date;
        this.stream = lesson.stream;
        this.type = lesson.type;
        this.group = lesson.group;
        this.checked = lesson.checked;
        this.lastLectureLesson = lesson.lastLectureLesson;
        this.lastPracticeLesson = lesson.lastPracticeLesson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lesson lesson = (Lesson) o;

        if (id != null ? !id.equals(lesson.id) : lesson.id != null) return false;
        if (name != null ? !name.equals(lesson.name) : lesson.name != null) return false;
        if (description != null ? !description.equals(lesson.description) : lesson.description != null) return false;
        if (createDate != null ? !createDate.equals(lesson.createDate) : lesson.createDate != null) return false;
        if (date != null ? !date.equals(lesson.date) : lesson.date != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Lesson{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", createDate='" + createDate + '\'' +
            ", date='" + date + '\'' +
            '}';
    }
}
