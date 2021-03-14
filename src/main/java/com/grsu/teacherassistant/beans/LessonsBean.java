package com.grsu.teacherassistant.beans;

import com.grsu.teacherassistant.beans.mode.RegistrationModeBean;
import com.grsu.teacherassistant.beans.utility.MenuBean;
import com.grsu.teacherassistant.constants.Constants;
import com.grsu.teacherassistant.dao.EntityDAO;
import com.grsu.teacherassistant.dao.LessonDAO;
import com.grsu.teacherassistant.dao.StreamDAO;
import com.grsu.teacherassistant.entities.Lesson;
import com.grsu.teacherassistant.entities.Note;
import com.grsu.teacherassistant.entities.Stream;
import com.grsu.teacherassistant.models.LessonType;
import com.grsu.teacherassistant.utils.FacesUtils;
import com.grsu.teacherassistant.utils.LessonUtils;
import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pavel Zaychick
 */
@Data
@ManagedBean(name = "lessonsBean")
@ViewScoped
public class LessonsBean implements Serializable {
    private final List<LessonType> lessonTypes =
        new ArrayList<>(Arrays.asList(LessonType.LECTURE, LessonType.PRACTICAL, LessonType.LAB, LessonType.EXAM));
    @ManagedProperty(value = "#{menuBean}")
    private MenuBean menuBean;
    @ManagedProperty(value = "#{registrationModeBean}")
    private RegistrationModeBean registrationModeBean;
    private List<Lesson> lessons;
    private Lesson selectedLesson;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private boolean closed;
    private boolean past = true;
    private boolean loadPreviousNotes = true;
    private Integer streamId;
    private Integer disciplineId;
    private Integer scheduleId;
    private Integer groupId;
    private Integer month;
    private LessonType type;
    private String newNote;
    private Map<Integer, String> streamNames;

    @PostConstruct
    private void init() {
        dateFrom = LocalDate.now().atStartOfDay();
        dateTo = LocalDate.now().plusMonths(1).atStartOfDay();
    }

    public void removeLesson(Lesson lesson) {
        EntityDAO.delete(lesson);
        lessons.remove(lesson);
    }

    public void toggleChecked() {
        selectedLesson.setChecked(!selectedLesson.getChecked());
        EntityDAO.update(selectedLesson);
    }

    public List<Lesson> getLessons() {
        if (lessons == null) {
            // workaround for loading all lessons of group
            if (groupId != null) {
                List<Stream> streams = StreamDAO.getAll();
                List<Stream> streamsOfGroup = streams.stream().filter(stream -> stream.getGroups().stream().anyMatch(group -> group.getId().equals(groupId))).collect(Collectors.toList());
                lessons = new ArrayList<>();
                for (Stream stream : streamsOfGroup) {
                    List<Lesson> streamLessons = LessonDAO.getAll(dateFrom, dateTo, closed, stream.getId(), past, disciplineId, scheduleId, null, type);
                    if (streamLessons != null) {
                        lessons.addAll(streamLessons);
                    }
                }
                lessons = lessons.stream().filter(lesson -> lesson.getGroup() == null || lesson.getGroup().getId().equals(groupId)).collect(Collectors.toList());
            } else {
                lessons = LessonDAO.getAll(dateFrom, dateTo, closed, streamId, past, disciplineId, scheduleId, groupId, type);
            }
            if (lessons != null) {
                lessons = lessons.stream().sorted((l1, l2) -> {
                    if (l1.getDate() == null && l2.getDate() == null) {
                        return 0;
                    }
                    if (l1.getDate() == null) {
                        return -1;
                    }
                    if (l2.getDate() == null) {
                        return 1;
                    }

                    if (!l1.getDate().toLocalDate().isEqual(l2.getDate().toLocalDate())) {
                        return l2.getDate().toLocalDate().compareTo(l1.getDate().toLocalDate());
                    }

                    if (l1.getSchedule() == null || l1.getSchedule().getEnd() == null) {
                        return -1;
                    }

                    if (l2.getSchedule() == null || l2.getSchedule().getEnd() == null) {
                        return 1;
                    }


                    LocalDateTime localDateTime1 = LocalDateTime.of(l1.getDate().toLocalDate(), l1.getSchedule().getEnd());
                    LocalDateTime localDateTime2 = LocalDateTime.of(l2.getDate().toLocalDate(), l2.getSchedule().getEnd());

                    if (localDateTime1.isAfter(LocalDateTime.now()) && localDateTime2.isAfter(LocalDateTime.now())) {
                        return localDateTime1.compareTo(localDateTime2);
                    }

                    return localDateTime2.compareTo(localDateTime1);
                }).collect(Collectors.toList());
            }
        }
        return lessons;
    }

    public void search() {
        lessons = null;
    }

    public Set<Map.Entry<Integer, String>> getStreams() {
        if (streamNames == null) {
            streamNames = StreamDAO.getNames();
        }
        return streamNames.entrySet();
    }

    public void changeMonth(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            dateFrom = LocalDate.now().plusMonths(-((Integer) event.getNewValue())).atStartOfDay();
            dateTo = LocalDate.now().plusMonths(1).atStartOfDay();
        }
    }

    public void changeDate(ValueChangeEvent event) {
        month = null;
    }

    public void openRegistrationMode() {
        registrationModeBean.initLesson(selectedLesson, lessons, true);
        menuBean.changeView("registrationMode");
        menuBean.hideMenu();
    }

    public void removeNote(Note note) {
        EntityDAO.delete(note);
        selectedLesson.getNotes().remove(note);
    }


    public void saveNote() {
        if (newNote != null && !newNote.isEmpty()) {
            Note note = new Note();
            note.setCreateDate(LocalDateTime.now());
            note.setDescription(newNote);
            note.setType(Constants.LESSON);
            note.setEntityId(selectedLesson.getId());
            selectedLesson.getNotes().add(note);
            EntityDAO.save(note);
        }
        newNote = null;
        FacesUtils.closeDialog("lessonNotesDialog");
    }

    public Lesson getFirstLesson() {
        List<Lesson> lessons = getLessons();
        if (lessons != null && lessons.size() > 0) {
            return lessons.get(0);
        }
        return null;
    }

    public Lesson getLastLecture(Lesson currentLesson) {
        if (currentLesson == null) {
            return null;
        }
        if (currentLesson.getLastLectureLesson() == null) {
            currentLesson.setLastLectureLesson(LessonUtils.getLastLesson(currentLesson, LessonType.LECTURE, loadPreviousNotes));
        }
        return currentLesson.getLastLectureLesson();
    }

    public Lesson getLastPractice(Lesson currentLesson) {
        if (currentLesson == null) {
            return null;
        }
        if (currentLesson.getLastPracticeLesson() == null) {
            currentLesson.setLastPracticeLesson(LessonUtils.getLastLesson(currentLesson, LessonType.PRACTICAL, loadPreviousNotes));
        }
        return currentLesson.getLastPracticeLesson();
    }
}
