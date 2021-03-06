package com.grsu.teacherassistant.beans.mode;

import com.grsu.teacherassistant.constants.Constants;
import com.grsu.teacherassistant.dao.EntityDAO;
import com.grsu.teacherassistant.dao.LessonDAO;
import com.grsu.teacherassistant.dao.StudentDAO;
import com.grsu.teacherassistant.entities.*;
import com.grsu.teacherassistant.models.*;
import com.grsu.teacherassistant.utils.FacesUtils;
import com.grsu.teacherassistant.utils.FileUtils;
import lombok.Data;
import org.primefaces.component.api.DynamicColumn;
import org.primefaces.event.CellEditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.grsu.teacherassistant.utils.PropertyUtils.AUTO_BACKUP_NEW_MODE_PROPERTY_NAME;
import static com.grsu.teacherassistant.utils.PropertyUtils.getProperty;

/**
 * @author Pavel Zaychick
 */
@ManagedBean(name = "lessonModeBean")
@ViewScoped
@Data
public class LessonModeBean implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(LessonModeBean.class);

    private Stream stream;
    private Lesson lesson;
    private List<LessonModel> lessons;
    private List<LessonModel> attestations;
    private List<LessonModel> exams;
    private LessonModel currentLesson;

    private List<Note> notes;
    private String newNote;
    private Integer entityId;

    private List<LessonStudentModel> students;
    private LessonStudentModel selectedLessonStudentModel;
    private LazyStudentDataModel studentsLazyModel;

    private LessonModel selectedLesson;

    private Integer selectedCell;
    private String selectedClientId;
    private String selectedType;
    private String selectedLessonType;

    private boolean registered;
    private boolean showAttestations = false;
    private boolean showSkips = true;

    public void initLessonMode() {
        initLessonStudents();
    }

    public void initLessonMode(Lesson lesson) {
        this.lesson = EntityDAO.get(Lesson.class, lesson.getId());
        this.stream = this.lesson.getStream();
        initLessonStudents();
    }

    public void clear() {
        stream = null;
        lessons = null;
        attestations = null;
        exams = null;

        notes = null;
        newNote = null;
        entityId = null;

        students = null;
        selectedLessonStudentModel = null;
        studentsLazyModel = null;

        selectedType = null;
        selectedCell = null;
        selectedClientId = null;
        selectedLessonType = null;

        selectedLesson = null;

        showAttestations = false;
        showSkips = true;
    }

    private void initLessonStudents() {
        if (Boolean.valueOf(getProperty(AUTO_BACKUP_NEW_MODE_PROPERTY_NAME))) {
            FileUtils.backupDatabase("OPEN_LESSON_MODE");
        }
        currentLesson = new LessonModel(lesson, true);

        List<Lesson> lessons = new ArrayList<>();
        Set<Student> studentSet = new HashSet<>();
        if (stream != null && lesson != null) {
            if (lesson.getGroup() != null) {
                studentSet.addAll(lesson.getGroup().getStudents());
                lessons = stream.getLessons().stream().filter(l -> l.getGroup() == null || (lesson.getGroup().equals(l.getGroup()))).collect(Collectors.toList());
            } else {
                stream.getGroups().stream().forEach(g -> studentSet.addAll(g.getStudents()));
                lessons = stream.getLessons();
            }
        }

        //int lessons
        this.lessons = lessons.stream()
            .filter(l -> Arrays.asList(LessonType.LECTURE, LessonType.PRACTICAL, LessonType.LAB).contains(l.getType()))
            .sorted((l1, l2) -> {
                if (l1.getDate().isAfter(l2.getDate())) return -1;
                if (l1.getDate().isBefore(l2.getDate())) return 1;
                return 0;
            })
            .map(LessonModel::new).collect(Collectors.toList());

        //init attestations
        this.attestations = lessons.stream()
            .filter(l -> LessonType.ATTESTATION.equals(l.getType()))
            .map(LessonModel::new).collect(Collectors.toList());
        attestations.forEach(a -> a.setNumber(attestations.indexOf(a) + 1));

        //init exams
        this.exams = lessons.stream()
            .filter(l -> LessonType.EXAM.equals(l.getType()))
            .map(LessonModel::new).collect(Collectors.toList());

        //init additional students
        List<LessonStudentModel> additionalStudents = StudentDAO.getAdditionalStudents(lesson.getId()).stream()
            .map(s -> new LessonStudentModel(s, stream, true)).collect(Collectors.toList());

        students = studentSet.stream().map(s -> new LessonStudentModel(s, stream)).collect(Collectors.toList());
        students.addAll(additionalStudents);
        students = students.stream().sorted(Comparator.comparing(LessonStudentModel::getName)).collect(Collectors.toList());

        Map<Integer, Map<String, Integer>> skipInfo = StudentDAO.getSkipInfo(stream.getId(), lesson.getId());
        students.stream().forEach(s -> {
            if (skipInfo.containsKey(s.getId())) {
                s.setTotalSkip(skipInfo.get(s.getId()).get(Constants.TOTAL_SKIP));
            }
        });

        studentsLazyModel = new LazyStudentDataModel(students);
    }

    public void initRegisteredDialog() {
        if (Constants.STUDENT_LESSON.equals(selectedType)) {
            selectedLesson = calculateSelectedLesson();
            registered = selectedLessonStudentModel.getStudent().getStudentLessons().get(selectedLesson.getId()).isRegistered();
        } else {
            selectedLesson = null;
        }
    }

    public boolean getIsStudentRegistered() {
        initRegisteredDialog();
        return registered;
    }

    public void initNotes() {
        notes = null;
        selectedLesson = calculateSelectedLesson();

        switch (selectedType) {
            case Constants.STUDENT_LESSON:
                StudentLesson sc = selectedLessonStudentModel.getStudent().getStudentLessons().get(selectedLesson.getId());
                if (sc != null) {
                    notes = sc.getNotes();
                    entityId = sc.getId();
                }
                break;
            case Constants.STUDENT:
                notes = selectedLessonStudentModel.getStudent().getNotes();
                entityId = selectedLessonStudentModel.getStudent().getId();
                break;
            case Constants.LESSON:
                notes = selectedLesson.getLesson().getNotes();
                entityId = selectedLesson.getId();
                break;
        }
    }

    private LessonModel calculateSelectedLesson() {
        if (selectedCell != null) {
            int column = selectedCell;
            if (Constants.OTHER.equals(selectedLessonType)) {
                return lessons.get(selectedCell);
            }
            if (Constants.ATTESTATION.equals(selectedLessonType)) {
                column--;
                if (showSkips) {
                    column--;
                }
                if (exams.size() > 0) {
                    column--;
                }
                return attestations.get(column);
            }
        }
        return null;
    }

    public void onCellEdit(CellEditEvent event) {
        if (!event.getColumn().getColumnKey().contains("attestation")) {
            Integer id = lessons.get(((DynamicColumn) event.getColumn()).getIndex()).getId();
            EntityDAO.update(studentsLazyModel.getRowData().getStudent().getStudentLessons().get(id));
        } else {

        }
    }

    public void changeAttestationMark(ValueChangeEvent event) {
        int attestationId = Integer.parseInt(String.valueOf(event.getComponent().getAttributes().get("attestationId")));
        int studentId = Integer.parseInt(String.valueOf(event.getComponent().getAttributes().get("studentId")));
        for (LessonStudentModel lessonStudentModel : students) {
            if (studentId == lessonStudentModel.getId()) {
                lessonStudentModel.updateAttestationMark(attestationId, (Mark) event.getNewValue());
                System.out.println("lessonModeTable:" + studentsLazyModel.getRowIndex() + ":averageAttestation");
                System.out.println(lessonStudentModel.getAverageAttestation());
                break;
            }
        }

    }

    public void updateAverageAttestation() {
        FacesUtils.update("lessonModeTable:" + studentsLazyModel.getRowIndex() + ":averageAttestation");
    }

    public void saveNote() {
        if (newNote != null && !newNote.isEmpty()) {
            Note note = new Note();
            note.setCreateDate(LocalDateTime.now());
            note.setDescription(newNote);
            note.setType(selectedType);
            note.setEntityId(entityId);
            notes.add(note);
            EntityDAO.save(note);
            newNote = null;
        }
        FacesUtils.closeDialog("notesDialog");
    }

    public void updateLessonSkipInfo(StudentLesson studentLesson) {
        studentLesson.setRegistered(!studentLesson.isRegistered());

        if (!studentLesson.isRegistered()) {
            studentLesson.setRegistrationTime(null);
            studentLesson.setRegistrationType(null);
        } else {
            studentLesson.setRegistrationTime(LocalTime.now());
            studentLesson.setRegistrationType(Constants.REGISTRATION_TYPE_MANUAL);
        }
        EntityDAO.save(studentLesson);
        selectedLessonStudentModel.updateSkips(stream);
    }

    public StudentLesson getCurrentStudentLesson() {
        initRegisteredDialog();
        if (selectedLessonStudentModel == null
            || selectedLessonStudentModel.getStudent() == null
            || selectedLessonStudentModel.getStudent().getStudentLessons() == null
            || selectedLessonStudentModel.getStudent().getStudentLessons().size() == 0
        ) {
            return null;
        }
        return selectedLessonStudentModel.getStudent().getStudentLessons().get(selectedLesson.getId());
    }

    public void saveRegisteredInfo() {
        boolean oldValue = selectedLessonStudentModel.getStudent().getStudentLessons().get(selectedLesson.getId()).isRegistered();
        if (oldValue != registered) {
            StudentLesson studentLesson = selectedLessonStudentModel.getStudent().getStudentLessons().get(selectedLesson.getId());
            studentLesson.setRegistered(registered);
            if (!registered) {
                studentLesson.setRegistrationTime(null);
                studentLesson.setRegistrationType(null);

                FacesUtils.execute("$('#" + selectedClientId.replaceAll("\\:", "\\\\\\\\:") + "').closest('td').addClass('skip');");
            } else {
                studentLesson.setRegistrationTime(LocalTime.now());
                studentLesson.setRegistrationType(Constants.REGISTRATION_TYPE_MANUAL);
                FacesUtils.execute("$('#" + selectedClientId.replaceAll("\\:", "\\\\\\\\:") + "').closest('td').removeClass('skip')");
            }
            EntityDAO.save(studentLesson);
        }
        FacesUtils.closeDialog("registeredDialog");
        selectedLesson = null;
    }

    public void closeDialog() {
        newNote = null;
        notes = null;
        selectedType = null;
        selectedCell = null;
        selectedLesson = null;
        LOGGER.info("selectedClientId " + selectedClientId);
        if (selectedClientId != null) {
            FacesUtils.update(selectedClientId);
        }
        selectedClientId = null;
    }

    public void removeNote(Note note) {
        EntityDAO.delete(note);
        notes.remove(note);
    }

    public void createAttestation() {
        Lesson lesson = new Lesson();
        lesson.setDate(LocalDateTime.now());
        lesson.setType(LessonType.ATTESTATION);
        lesson.setStream(this.stream);
        lesson.setNotes(new ArrayList<>());
        lesson.setIndex(LessonDAO.getNextIndex(stream.getId(), LessonType.ATTESTATION, null));

        EntityDAO.add(lesson);
        stream.getLessons().add(lesson);

        Set<Student> students = new HashSet<>();
        stream.getGroups().forEach(g -> students.addAll(g.getStudents()));
        List<StudentLesson> studentLessons = new ArrayList<>();
        students.forEach(s -> {
            StudentLesson sc = new StudentLesson();
            sc.setStudent(s);
            sc.setLesson(lesson);
            sc.setNotes(new ArrayList<>());
            studentLessons.add(sc);
            s.getStudentLessons().put(lesson.getId(), sc);
        });
        EntityDAO.add(new ArrayList<>(studentLessons));
        lesson.setStudentLessons(new HashMap<>());
        studentLessons.stream().forEach(sc -> lesson.getStudentLessons().put(sc.getStudentId(), sc));
        attestations.add(new LessonModel(lesson));
        attestations.forEach(a -> a.setNumber(attestations.indexOf(a) + 1));

        initLessonMode(this.lesson);
    }

    public int frozenColumns() {
        int frozenColumns = 2;
        if (showAttestations) {
            frozenColumns += attestations.size();
            if (attestations.size() > 1) {
                frozenColumns++;
            }
            if (exams.size() > 0) {
                frozenColumns++;
            }
        }
        return frozenColumns;
    }

    public void removeAttestation(LessonModel lesson) {
        EntityDAO.delete(lesson.getLesson());
        attestations.remove(lesson);
        attestations.forEach(a -> a.setNumber(attestations.indexOf(a) + 1));
        students.forEach(LessonStudentModel::updateAverageAttestation);
        stream.getLessons().remove(lesson.getLesson());
    }

}
