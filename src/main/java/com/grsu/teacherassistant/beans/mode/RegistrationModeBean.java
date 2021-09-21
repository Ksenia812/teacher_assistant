package com.grsu.teacherassistant.beans.mode;

import com.grsu.teacherassistant.beans.AlarmBean;
import com.grsu.teacherassistant.beans.NotificationSettingsBean;
import com.grsu.teacherassistant.beans.utility.*;
import com.grsu.teacherassistant.constants.Constants;
import com.grsu.teacherassistant.dao.DisciplineDAO;
import com.grsu.teacherassistant.dao.EntityDAO;
import com.grsu.teacherassistant.dao.LessonDAO;
import com.grsu.teacherassistant.dao.StudentDAO;
import com.grsu.teacherassistant.entities.*;
import com.grsu.teacherassistant.models.*;
import com.grsu.teacherassistant.push.resources.PushMessage;
import com.grsu.teacherassistant.serial.SerialStatus;
import com.grsu.teacherassistant.utils.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.primefaces.component.api.DynamicColumn;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.grsu.teacherassistant.utils.FacesUtils.closeDialog;
import static com.grsu.teacherassistant.utils.FacesUtils.update;
import static com.grsu.teacherassistant.utils.PropertyUtils.AUTO_BACKUP_NEW_MODE_PROPERTY_NAME;
import static com.grsu.teacherassistant.utils.PropertyUtils.getProperty;

@ManagedBean(name = "registrationModeBean")
@ViewScoped
@Data
public class RegistrationModeBean implements Serializable, SerialListenerBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationModeBean.class);

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value = "#{lessonModeBean}")
    private LessonModeBean lessonModeBean;

    @ManagedProperty(value = "#{studentModeBean}")
    private StudentModeBean studentModeBean;

    @ManagedProperty(value = "#{serialBean}")
    private SerialBean serialBean;

    @ManagedProperty(value = "#{imageBean}")
    private ImageBean imageBean;

    @ManagedProperty(value = "#{localeBean}")
    private LocaleBean localeBean;

    @ManagedProperty(value = "#{alarmBean}")
    private AlarmBean alarmBean;

    @ManagedProperty(value = "#{notificationSettingsBean}")
    private NotificationSettingsBean notificationSettingsBean;

    private Lesson initialData;
    private int totalSkips;
    private List<Lesson> orderedLessons;
    private Lesson selectedLesson;
    private Student processedStudent;
    //private List<StudentLesson> processedStudentSkips;

    private List<Student> presentStudents;
    private List<Student> absentStudents = new ArrayList<>();
    private List<Student> additionalStudents;

    private List<Student> allStudents;
    private List<Student> filteredAllStudents;

    private Set<Student> lessonStudents = new HashSet<>();


    private List<LessonStudentModel> lessonPresentStudents;
    private List<LessonStudentModel> lessonAbsentStudents;

    private LazyStudentDataModel presentStudentsLazyModel;
    private LazyStudentDataModel absentStudentsLazyModel;
    private LazyStudentDataModel selectedStudentLazyModel;

    private List<LessonStudentModel> selectedPresentStudents;
    private List<LessonStudentModel> selectedAbsentStudents;
    private List<LessonStudentModel> students;

    private boolean selectedAllPresentStudents;
    private boolean selectedAllAbsentStudents;

    private Map<Integer, Map<String, Integer>> skipInfo;

    private long timer;
    private boolean camera = false;
    private List<Note> notes;

    private boolean reRegistration;
    private boolean fastRegistration;
    private Lesson lastLesson;
    private boolean studentNotExist;
    private Stream stream;
    private List<LessonModel> lessons;
    private List<LessonModel> attestations;
    private List<LessonModel> exams;
    private String newNotification;
    @Setter(AccessLevel.NONE)
    private LessonStudentModel selectedStudent;
    private String newNote;
    private String newLessonNote;
    private boolean nextLessonAvailable = false;
    private Lesson lastLecture;
    private Lesson lastPractice;

    private Integer disciplineId;

    //gets list of students skips dates and lessons and formats for ui
    public List<String> getStudentTotalSkips() {
        List<StudentSkip> processedStudentSkips = StudentDAO.getStudentSkipsDates(processedStudent.getId(), selectedLesson.getId(), disciplineId);
        List<String> skips = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        for (StudentSkip skip : processedStudentSkips) {
            String date = formatter.format(skip.getSkipDate());
            String lessonType = convertLessonType(skip.getLessonType());

            skips.add(String.format("%s %s", date, lessonType));
        }

        return skips;
    }

    public List<String> getStudentTotalSkipsWithAdditionalLessons() {
        List<StudentSkip> processedStudentSkips = StudentDAO.getStudentSkipsDates(processedStudent.getId(), selectedLesson.getId(), disciplineId);
        List<AdditionalLesson> processedStudentAdditionalLessons = StudentDAO.getStudentAdditionalLessonsInfo(processedStudent.getId(), disciplineId);

        long additionalLectures = processedStudentAdditionalLessons.stream()
            .filter(s -> s.getLessonType().equals(LessonType.LECTURE))
            .count();

        long additionalPracticals = processedStudentAdditionalLessons.stream()
            .filter(s -> s.getLessonType().equals(LessonType.PRACTICAL))
            .count();

        long additionalLabs = processedStudentAdditionalLessons.stream()
            .filter(s -> s.getLessonType().equals(LessonType.LAB))
            .count();

        List<StudentSkip> lectureSkips = processedStudentSkips.stream()
            .filter(s -> s.getLessonType().equals(LessonType.LECTURE))
            .collect(Collectors.toList());

        List<StudentSkip> practicalSkips = processedStudentSkips.stream()
            .filter(s -> s.getLessonType().equals(LessonType.PRACTICAL))
            .collect(Collectors.toList());

        List<StudentSkip> labSkips = processedStudentSkips.stream()
            .filter(s -> s.getLessonType().equals(LessonType.LAB))
            .collect(Collectors.toList());

        lectureSkips = (List<StudentSkip>) removeElements(lectureSkips, additionalLectures);
        practicalSkips = (List<StudentSkip>) removeElements(practicalSkips, additionalPracticals);
        labSkips = (List<StudentSkip>) removeElements(labSkips, additionalLabs);

        processedStudentSkips = new ArrayList<>();
        processedStudentSkips.addAll(lectureSkips);
        processedStudentSkips.addAll(practicalSkips);
        processedStudentSkips.addAll(labSkips);

        List<String> skips = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        for (StudentSkip skip : processedStudentSkips) {
            String date = formatter.format(skip.getSkipDate());
            String lessonType = convertLessonType(skip.getLessonType());

            skips.add(String.format("%s %s", date, lessonType));
        }


        return skips;
    }

    public List<String> getStudentSkipsDatesByLessonType(int lessonTypeCode) {
        List<Date> processedStudentSkipsDates = StudentDAO.getStudentSkipsByLessonType(processedStudent.getId(), lessonTypeCode, selectedLesson.getId(), disciplineId);

        if (processedStudentSkipsDates.size() > 0) {
            List<String> skips = new ArrayList<>();
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

            for (Date processedStudentSkipsDate : processedStudentSkipsDates) {
                skips.add(formatter.format(processedStudentSkipsDate));
            }

            return skips;
        }

        return null;
    }

    public int getAbsentStudentsPercent() {
        if (lessonStudents.size() != 0) {
            return (int) Math.round(1.0 * absentStudents.size() / lessonStudents.size() * 100);
        }
        return 0;
    }

    public void initLesson(Lesson lesson, boolean forceLoading) {
        this.initLesson(lesson, null, forceLoading);
    }

    public void initLesson(Lesson lesson, List<Lesson> otherLessons, boolean forceLoading) {
        runBackup("OPEN_REGISTRATION_MODE");
        this.orderedLessons = otherLessons;
        initialData = lesson;
        processedStudent = null;
        notes = null;
        alarmBean.setAlarms();
        serialBean.setCurrentListener(this);
        serialBean.startRecord();

        if (lesson != null) {
            clear();
            this.selectedLesson = EntityDAO.get(Lesson.class, lesson.getId());
            this.disciplineId = DisciplineDAO.getDisciplineByLessonId(this.selectedLesson.getId());
            calculateTimer();
            initStudents();
            initAllStudents();
            initLessonStudents();
            initAdditionalStudents();

            if (selectedLesson.getStream() != null) {
                skipInfo = StudentDAO.getSkipInfo(selectedLesson.getStream().getId(), selectedLesson.getId());
                lessonAbsentStudents = new ArrayList<>();
                lessonPresentStudents = new ArrayList<>();

                updateLessonStudents();

                absentStudentsLazyModel = new LazyStudentDataModel(lessonAbsentStudents);
                presentStudentsLazyModel = new LazyStudentDataModel(lessonPresentStudents);

                lastLecture = LessonUtils.getLastLesson(selectedLesson, LessonType.LECTURE, forceLoading);
                lastPractice = LessonUtils.getLastLesson(selectedLesson, LessonType.PRACTICAL, forceLoading);
            }

            initLastLesson();
            isNextLessonAvailable();
        }
    }

    private void initLastLesson() {
        fastRegistration = false;
        if (lessonPresentStudents.size() == 0) {
            lastLesson = null;
            List<Lesson> lessons = LessonDAO.getAll(selectedLesson.getDate(), selectedLesson.getDate(), false, selectedLesson.getStream().getId());
            for (Lesson lesson : lessons) {
                if (selectedLesson.getSchedule().getBegin().isAfter(lesson.getSchedule().getBegin())) {
                    lastLesson = lesson;
                    break;
                }
            }

            if (lastLesson != null) {
                fastRegistration = true;
            }
        }
    }

    public void fastRegistration() {
        selectedLesson.getStudentLessons().values().forEach(sl -> {
            if (!sl.isRegistered()) {
                StudentLesson studentLesson = lastLesson.getStudentLessons().get(sl.getStudentId());
                if (studentLesson != null && studentLesson.isRegistered()) {
                    sl.setRegistered(studentLesson.isRegistered());
                    sl.setRegistrationTime(LocalTime.now());
                    sl.setRegistrationType("MANUAL");
                }
            }
        });
        EntityDAO.update(new ArrayList<>(selectedLesson.getStudentLessons().values()));
        fastRegistration = false;
        initLesson(selectedLesson, orderedLessons, true);
    }

    public Lesson getNextLesson() {
        if (!isNextLessonAvailable()) {
            return null;
        }
        return orderedLessons.get(orderedLessons.indexOf(initialData) + 1);
    }

    public boolean isNextLessonAvailable() {
        return orderedLessons != null && orderedLessons.size() > orderedLessons.indexOf(initialData) + 1;
    }

    public void returnToLessons() {
        clear();
        sessionBean.setActiveView("lessons");
    }

    public void openLessonMode() {
        lessonModeBean.setStream(selectedLesson.getStream());
        lessonModeBean.setLesson(selectedLesson);
        sessionBean.setActiveView("lessonMode");
        lessonModeBean.initLessonMode();
    }

    public void openPhotoMode() {
        lessonModeBean.setStream(selectedLesson.getStream());
        lessonModeBean.setLesson(selectedLesson);
        sessionBean.setActiveView("photoMode");
        lessonModeBean.initLessonMode();
    }

    public void clear() {
        selectedLesson = null;
        processedStudent = null;
        presentStudents = null;
        absentStudents = null;
        lessonStudents = null;
        allStudents = null;
        filteredAllStudents = null;
        skipInfo = null;
        additionalStudents = null;
        timer = 0;
        camera = false;
        notes = null;
        reRegistration = false;
        studentNotExist = false;
    }

    private void initStudents() {
        if (selectedLesson == null || selectedLesson.getStream() == null) {
            presentStudents = null;
            absentStudents = null;
        } else {
            presentStudents = new ArrayList<>();
            absentStudents = new ArrayList<>();
            for (StudentLesson studentLesson : selectedLesson.getStudentLessons().values()) {
                if (studentLesson.isRegistered()) {
                    presentStudents.add(studentLesson.getStudent());
                } else {
                    absentStudents.add(studentLesson.getStudent());
                }
            }
        }
    }

    public void initAllStudents() {
        List<Student> allStudents = new ArrayList<>(sessionBean.getStudents());
        if (presentStudents != null) {
            allStudents.removeAll(presentStudents);
        }
        if (absentStudents != null) {
            allStudents.removeAll(absentStudents);
        }
        this.allStudents = allStudents;
    }

    private void initLessonStudents() {
        lessonStudents = new HashSet<>();
        if (selectedLesson.getStream() != null) {
            if (selectedLesson.getGroup() != null) {
                lessonStudents = new HashSet<>(selectedLesson.getGroup().getStudents());
            } else {
                for (Group group : selectedLesson.getStream().getGroups()) {
                    lessonStudents.addAll(group.getStudents());
                }
            }
        }
    }

    private void initAdditionalStudents() {
        additionalStudents = new ArrayList<>();
        if (lessonStudents != null && presentStudents != null) {
            additionalStudents = new ArrayList<>(this.presentStudents);
            additionalStudents.removeAll(this.lessonStudents);
        }
    }

    public void showAdditionalStudents() {
        FacesUtils.showDialog("amountOfAdditionalStudents");
    }

    public void exitAdditionalStudentsDialog() {
        FacesUtils.closeDialog("amountOfAdditionalStudents");
    }

    public void runBackup(String reason) {
        if (Boolean.valueOf(getProperty(AUTO_BACKUP_NEW_MODE_PROPERTY_NAME))) {
            FileUtils.backupDatabase(reason);
        }
    }

    public List<Student> getPraepostors(){
        List<Student> praepostors = new ArrayList<>();
        if (selectedLesson.getType().equals(LessonType.LECTURE)) {
            praepostors = selectedLesson.getStream().getGroups().stream()
                .map(Group::getPraepostor).collect(Collectors.toList());
        }
        else {
            praepostors.add(selectedLesson.getGroup().getPraepostor());
        }
        return praepostors;
    }

    //TODO add special sign to praepostor
    @Override
    public SerialStatus process(String uid, String name) {
        final long t = System.currentTimeMillis();
        LOGGER.info("==> process(); uid = " + uid);
        reRegistration = false;
        studentNotExist = false;
        Student student = EntityUtils.getPersonByUid(absentStudents, uid);
        if (student == null) {
            student = EntityUtils.getPersonByUid(presentStudents, uid);
            if (student != null) {
                reRegistration = true;
                LOGGER.info("Student not registered. Reason: Uid[ " + uid + " ] already registered.");
            } else {
                student = EntityUtils.getPersonByUid(allStudents, uid);
            }
        }
        if (student == null) {
            LOGGER.info("Student not registered. Reason: Uid[ " + uid + " ] not exist in database.");
            studentNotExist = true;
            student = new Student();
            student.setCardUid(uid);
            if (name != null) {
                String[] names = name.split(" ");
                if (names.length >= 0) {
                    student.setLastName(names[0]);
                }
                if (names.length >= 1) {
                    student.setFirstName(names[1]);
                }
                if (names.length >= 2) {
                    student.setPatronymic(names[2]);
                }
            }
        }
        LOGGER.info("<== process(); reRegistration = " + reRegistration + "; studentNotExist " + studentNotExist + "; student = " + student + "; " + (System.currentTimeMillis() - t));
        return processStudent(student);
    }

    private SerialStatus processStudent(Student student) {
        final long t = System.currentTimeMillis();
        LOGGER.info("==> processStudent();");
        if (!reRegistration && !studentNotExist) {
            presentStudents.add(student);
            if (absentStudents.contains(student)) {
                absentStudents.remove(student);
            } else {
                additionalStudents.add(student);
            }

            if (student.getStudentLessons() != null) {
                StudentLesson studentLesson = student.getStudentLessons().get(selectedLesson.getId());
                if (studentLesson == null) {
                    studentLesson = new StudentLesson();
                    studentLesson.setStudent(student);
                    studentLesson.setLesson(selectedLesson);
                    studentLesson.setRegistered(true);
                    studentLesson.setRegistrationTime(LocalTime.now());
                    studentLesson.setRegistrationType(Constants.REGISTRATION_TYPE_AUTOMATIC);
                    studentLesson.setNotes(new ArrayList<>());
                    EntityDAO.add(studentLesson);
                    student.getStudentLessons().put(selectedLesson.getId(), studentLesson);
                    selectedLesson.getStudentLessons().put(studentLesson.getStudent().getId(), studentLesson);
                } else {
                    studentLesson.setRegistered(true);
                    studentLesson.setRegistrationTime(LocalTime.now());
                    studentLesson.setRegistrationType(Constants.REGISTRATION_TYPE_AUTOMATIC);
                    studentLesson.setNotes(new ArrayList<>());
                    EntityDAO.update(studentLesson);
                    updateSkipInfo(Arrays.asList(student));
                }
            }

            selectStudent(student);
            updateLessonStudents();
            FacesUtils.push("/register", new PushMessage(processedStudent.getCardUid()));
            checkStudentNotifications(student);
            pushStudentDesktopNotification();
            LOGGER.info("Student registered");
            LOGGER.info("<== processStudent(); registered = true" + (System.currentTimeMillis() - t));
            return SerialStatus.INFO;
        } else {
            selectStudent(student);
            FacesUtils.push("/register", new PushMessage(processedStudent.getCardUid()));
            pushStudentDesktopNotification();
            LOGGER.info("Student not registered");
            LOGGER.info("<== processStudent(); registered = false; " + (System.currentTimeMillis() - t));
            if (reRegistration) {
                return SerialStatus.WARN;
            } else {
                return SerialStatus.ERROR;
            }
        }
    }

    public void addStudent(Student student) {
        reRegistration = false;
        studentNotExist = false;
        if (!selectedLesson.getStudentLessons().containsKey(student.getId())) {
            student = EntityDAO.get(Student.class, student.getId());
        }
        presentStudents.add(student);
        if (absentStudents.contains(student)) {
            absentStudents.remove(student);
        } else {
            additionalStudents.add(student);
            allStudents.remove(student);
            if (filteredAllStudents != null) {
                filteredAllStudents.remove(student);
            }
        }

        if (student.getStudentLessons() != null) {
            StudentLesson studentLesson = student.getStudentLessons().get(selectedLesson.getId());
            if (studentLesson == null) {
                studentLesson = new StudentLesson();
                studentLesson.setStudent(student);
                studentLesson.setLesson(selectedLesson);
                studentLesson.setRegistered(true);
                studentLesson.setRegistrationTime(LocalTime.now());
                studentLesson.setRegistrationType(Constants.REGISTRATION_TYPE_MANUAL);
                studentLesson.setNotes(new ArrayList<>());
                EntityDAO.add(studentLesson);
                student.getStudentLessons().put(selectedLesson.getId(), studentLesson);
                selectedLesson.getStudentLessons().put(studentLesson.getStudent().getId(), studentLesson);
            } else {
                studentLesson.setRegistered(true);
                studentLesson.setRegistrationTime(LocalTime.now());
                studentLesson.setRegistrationType(Constants.REGISTRATION_TYPE_MANUAL);
                studentLesson.setNotes(new ArrayList<>());
                EntityDAO.update(studentLesson);
                updateSkipInfo(Arrays.asList(student));
            }
            checkStudentNotifications(student);
        }

        selectStudent(student);
        updateLessonStudents();
        FacesUtils.execute("PF('aStudentsTable').clearFilters()");
        FacesUtils.execute("PF('pStudentsTable').clearFilters()");
    }

    public void removeStudent(Student student) {
        StudentLesson studentLesson = student.getStudentLessons().get(selectedLesson.getId());
        if (lessonStudents.contains(student)) {

            studentLesson.setRegistrationType(null);
            studentLesson.setRegistrationTime(null);
            studentLesson.setRegistered(false);
            EntityDAO.update(studentLesson);
            absentStudents.add(student);
            updateSkipInfo(Arrays.asList(student));
        } else {
            student.getStudentLessons().remove(selectedLesson.getId());
            selectedLesson.getStudentLessons().remove(student.getId());
            EntityDAO.delete(studentLesson);
            allStudents.add(student);
        }

        presentStudents.remove(student);
        additionalStudents.remove(student);
        updateLessonStudents();

        FacesUtils.execute("PF('aStudentsTable').clearFilters()");
        FacesUtils.execute("PF('pStudentsTable').clearFilters()");
    }

    /* LESSON STUDENTS TABLES */

    public void addAbsentStudents() {
        if (selectedAbsentStudents != null && selectedAbsentStudents.size() > 0) {
            List<Student> selectedStudents = null;
            if (selectedAllAbsentStudents) {
                selectedStudents = lessonAbsentStudents.stream().map(LessonStudentModel::getStudent).collect(Collectors.toList());
            } else {
                selectedStudents = selectedAbsentStudents.stream().map(LessonStudentModel::getStudent).collect(Collectors.toList());
            }

            List<StudentLesson> studentLessonList = new ArrayList<>();
            for (Student student : selectedStudents) {
                StudentLesson studentLesson = student.getStudentLessons().get(selectedLesson.getId());
                studentLesson.setRegistered(true);
                studentLesson.setRegistrationTime(LocalTime.now());
                studentLesson.setRegistrationType(Constants.REGISTRATION_TYPE_MANUAL);
                studentLessonList.add(studentLesson);
            }
            EntityDAO.update(new ArrayList<>(studentLessonList));
            updateSkipInfo(selectedStudents);

            presentStudents.addAll(selectedStudents);
            absentStudents.removeAll(selectedStudents);
            allStudents.removeAll(selectedStudents);

            selectedAbsentStudents.clear();

            updateLessonStudents();
        }

        selectStudent(null);
        FacesUtils.execute("PF('aStudentsTable').clearFilters()");
        FacesUtils.execute("PF('pStudentsTable').clearFilters()");
    }

    public void removePresentStudents() {
        if (selectedPresentStudents != null && selectedPresentStudents.size() > 0) {
            List<Student> selectedStudents = null;
            if (selectedAllPresentStudents) {
                selectedStudents = lessonPresentStudents.stream().map(LessonStudentModel::getStudent).collect(Collectors.toList());
            } else {
                selectedStudents = selectedPresentStudents.stream().map(LessonStudentModel::getStudent).collect(Collectors.toList());
            }


            List<StudentLesson> updateStudentLessonList = new ArrayList<>();
            List<StudentLesson> removeStudentLessonList = new ArrayList<>();

            for (Student student : selectedStudents) {
                StudentLesson studentLesson = student.getStudentLessons().get(selectedLesson.getId());

                if (lessonStudents.contains(student)) {

                    studentLesson.setRegistrationType(null);
                    studentLesson.setRegistrationTime(null);
                    studentLesson.setRegistered(false);
                    updateStudentLessonList.add(studentLesson);
                    absentStudents.add(student);
                } else {
                    student.getStudentLessons().remove(selectedLesson.getId());
                    selectedLesson.getStudentLessons().remove(student.getId());
                    allStudents.add(student);
                    removeStudentLessonList.add(studentLesson);
                }
            }
            EntityDAO.update(new ArrayList<>(updateStudentLessonList));
            EntityDAO.delete(new ArrayList<>(removeStudentLessonList));
            updateSkipInfo(selectedStudents);

            presentStudents.removeAll(selectedStudents);
            additionalStudents.removeAll(selectedStudents);
            selectedPresentStudents.clear();

            updateLessonStudents();
        }

        FacesUtils.execute("PF('aStudentsTable').clearFilters()");
        FacesUtils.execute("PF('pStudentsTable').clearFilters()");
    }

    private void updateSkipInfo(List<Student> students) {
        final long t = System.currentTimeMillis();
        LOGGER.info("==> updateSkipInfo();");
        List<Integer> studentIds = students.stream().map(Student::getId).collect(Collectors.toList());

        List<SkipInfo> studentSkipInfo = StudentDAO.getStudentSkipInfo(studentIds, selectedLesson.getStream().getId(), selectedLesson.getId());
        for (Integer id : studentIds) {
            skipInfo.remove(id);
        }

        if (studentSkipInfo != null && studentSkipInfo.size() > 0) {
            for (SkipInfo si : studentSkipInfo) {

                Map<String, Integer> studentSkipInfoMap = skipInfo.get(si.getStudentId());
                if (studentSkipInfoMap == null) {
                    studentSkipInfoMap = new HashMap<>();
                    studentSkipInfoMap.put(Constants.TOTAL_SKIP, 0);
                    skipInfo.put(si.getStudentId(), studentSkipInfoMap);
                }

                studentSkipInfoMap.put(si.getLessonType().getKey(), si.getCount());
                int total = studentSkipInfoMap.get(Constants.TOTAL_SKIP);
                studentSkipInfoMap.put(Constants.TOTAL_SKIP, total + si.getCount());
            }
        }

        LOGGER.info("<== updateSkipInfo(); " + (System.currentTimeMillis() - t));
    }

    private void generateLessonStudents(List<LessonStudentModel> lessonStudentModelList, List<Student> students) {
        lessonStudentModelList.clear();
        for (Student st : students) {
            LessonStudentModel lessonStudentModel = new LessonStudentModel(st);
            lessonStudentModel.setRegistrationTime(
                st.getStudentLessons().get(selectedLesson.getId()).getRegistrationTime());
            lessonStudentModel.setAdditional(!lessonStudents.contains(st));
            Map<String, Integer> stSkipInfo = skipInfo.get(st.getId());
            if (stSkipInfo != null) {
                lessonStudentModel.setTotalSkip(stSkipInfo.get(Constants.TOTAL_SKIP));
                lessonStudentModel.setLectureSkip(stSkipInfo.get(LessonType.LECTURE.getKey()));
                lessonStudentModel.setPracticalSkip(stSkipInfo.get(LessonType.PRACTICAL.getKey()));
                lessonStudentModel.setLabSkip(stSkipInfo.get(LessonType.LAB.getKey()));
            }

            lessonStudentModelList.add(lessonStudentModel);
        }
    }

    private void updateLessonStudents() {
        final long t = System.currentTimeMillis();
        LOGGER.info("==> updateLessonStudents();");
        generateLessonStudents(lessonAbsentStudents, absentStudents);
        generateLessonStudents(lessonPresentStudents, presentStudents);
        LOGGER.info("<== updateLessonStudents(); " + (System.currentTimeMillis() - t));
    }

    public void addLessonStudent(LessonStudentModel lessonStudentModel) {
        addStudent(lessonStudentModel.getStudent());
    }

    public void removeLessonStudent(LessonStudentModel lessonStudentModel) {
        removeStudent(lessonStudentModel.getStudent());
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {

        }
    }

    public String getTotalStudentSkips(Student student) {
        LocaleUtils localeUtils = new LocaleUtils(localeBean.getLocale());
        Map<String, Integer> studentSkipInfoMap = skipInfo.get(student.getId());
        if (studentSkipInfoMap != null) {
            Integer skipsAmount = studentSkipInfoMap.get(Constants.TOTAL_SKIP);
            this.totalSkips = skipsAmount;
            return skipsAmount.toString();
        }
        return localeUtils.getMessage("lesson.visit.noSkip");
    }

    public int getTotalStudentSkipsAmount(Student student) {
        Map<String, Integer> studentSkipInfoMap = skipInfo.get(student.getId());
        if (studentSkipInfoMap != null) {
            return studentSkipInfoMap.get(Constants.TOTAL_SKIP);
        }
        return 0;
    }


    public String getTotalSkipsAdditionalLessons(Student student) {
        int totalStudentSkipsAmount = getTotalStudentSkipsAmount(student);
        int studentAdditionalLessons = getStudentAdditionalLessons(student).size();
        if (studentAdditionalLessons > totalStudentSkipsAmount) {
            return "0";
        }
            return String.valueOf(getTotalStudentSkipsAmount(student) - getStudentAdditionalLessons(student).size());
    }

    public int getStudentSkipsByLessonType(Student student, int lessonTypeCode) {
        Map<String, Integer> studentSkipInfoMap = skipInfo.get(student.getId());
        if (studentSkipInfoMap != null && LessonType.getLessonTypeByCode(lessonTypeCode) != null) {
            Integer skipsAmount = studentSkipInfoMap.get(LessonType.getLessonTypeByCode(lessonTypeCode).getKey());
            return skipsAmount == null ? 0 : skipsAmount;
        }
        return 0;
    }

    public String getStudentSkip(Student student) {
        LocaleUtils localeUtils = new LocaleUtils(localeBean.getLocale());
        Map<String, Integer> studentSkipInfoMap = skipInfo.get(student.getId());
        if (studentSkipInfoMap != null) {
            Integer total = studentSkipInfoMap.get(Constants.TOTAL_SKIP);
            Integer lecture = studentSkipInfoMap.get(LessonType.LECTURE.getKey());
            Integer practical = studentSkipInfoMap.get(LessonType.PRACTICAL.getKey());
            Integer lab = studentSkipInfoMap.get(LessonType.LAB.getKey());

            String skip = (lecture == null ? "0" : lecture) + "/" +
                (practical == null ? "0" : practical) + "/" +
                (lab == null ? "0" : lab);

            return localeUtils.getMessage("label.skips") + ": " + total + "&nbsp;&nbsp;&nbsp;" + skip;
        }
        return localeUtils.getMessage("lesson.visit.noSkip");
    }

    public String getAdditionLessonInfo(Student student) {
        int studentAdditionalLessonsAmount = StudentDAO.getStudentAdditionalLessonsAmount(student.getId(), disciplineId);
        String additionalLessonCountInfo = studentAdditionalLessonsAmount == 0 ? "" : String.format(" +%d", studentAdditionalLessonsAmount);
        return additionalLessonCountInfo;
    }

    public List<String> getStudentAdditionalLessons(Student student) {
        List<AdditionalLesson> studentAdditionalLessons = StudentDAO.getStudentAdditionalLessonsInfo(student.getId(), disciplineId);
        List<String> studentAdditionalLessonsInfo = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        for (AdditionalLesson l : studentAdditionalLessons) {
            studentAdditionalLessonsInfo.add(String.format("%s %s %s",
                formatter.format(l.getDate()), convertLessonType(l.getLessonType()), l.getGroupName()));
        }

            return studentAdditionalLessonsInfo;
    }

    public void onStudentRowSelect(SelectEvent event) {
        selectStudent(((LessonStudentModel) event.getObject()).getStudent());
    }

    public void onStudentRowDblClckSelect(SelectEvent event) {
        lessonModeBean.setLesson(selectedLesson);
        lessonModeBean.setStream(selectedLesson.getStream());
        sessionBean.setActiveView("studentMode");
        if (event.getObject() instanceof Student) {
            studentModeBean.initStudentMode((Student) event.getObject(), selectedLesson.getStream());
        } else {
            studentModeBean.initStudentMode(((LessonStudentModel) event.getObject()).getStudent(), selectedLesson.getStream());
        }
    }

    public void onPresentStudentsSelect(ToggleSelectEvent event) {
        selectedAllPresentStudents = event.isSelected();
    }

    public void onAbsentStudentsSelect(ToggleSelectEvent event) {
        selectedAllAbsentStudents = event.isSelected();
    }

    public void calculateTimer() {
        timer = 0;

        LocalDateTime date = selectedLesson.getDate();
        LocalTime begin = selectedLesson.getSchedule().getBegin();
        LocalDateTime beginDate = date.plus(begin.toNanoOfDay(), ChronoUnit.NANOS);
        LocalDateTime now = LocalDateTime.now();
        if (beginDate.isAfter(now)) {
            timer = now.until(beginDate, ChronoUnit.SECONDS);
        }
        if (timer == 0) {
            LocalTime end = selectedLesson.getSchedule().getEnd();
            LocalDateTime endDate = date.plus(end.toNanoOfDay(), ChronoUnit.NANOS);
            if (endDate.isAfter(now)) {
                timer = now.until(endDate, ChronoUnit.SECONDS);
            }
        }
    }

    public void exitStudents() {
        setFilteredAllStudents(null);
        update("views");
        closeDialog("addStudentsDialog");
    }

    private void selectStudent(Student student) {
        if (student != null) {
            processedStudent = student;
            notes = new ArrayList<>();
            if (processedStudent.getNotes() != null) {
                notes.addAll(processedStudent.getNotes());
            }
            if (processedStudent.getStudentLessons() != null) {
                processedStudent.getStudentLessons().values().forEach(sc -> notes.addAll(sc.getNotes()));
            }
        } else {
            processedStudent = null;
            notes = null;
        }
    }

    /**
     * Генерация всплывабщего уведомления с информацией о студенте
     *
     * @return уведомление
     */
    private Notification createStudentDesktopNotification() {
        LocaleUtils localeUtils = new LocaleUtils(localeBean.getLocale());
        Notification notification = new Notification();
        notification.setTimeout(3000);

        if (!studentNotExist) {
            String skipsStr = String.format("%s (%s:%s/%s) +%s",
                getTotalSkipsAdditionalLessons(processedStudent), getTotalStudentSkips(processedStudent),
                getStudentSkipsByLessonType(processedStudent, LessonType.LECTURE.getCode()), getStudentSkipsByLessonType(processedStudent, LessonType.PRACTICAL.getCode()),
                getAdditionLessonInfo(processedStudent));

            notification.setBody(skipsStr);
            notification.setTitle(processedStudent.getLastName() + "\n" + processedStudent.getFirstName());
        } else {
            notification.setBody(localeUtils.getMessage("label.studentNotExist"));
            notification.setTitle(processedStudent.getCardUid());
        }

        notification.setImage(imageBean.getImagePath(processedStudent.getCardUid()));
        return notification;
    }

    /**
     * Отправка сгенерированного уведомления клиенту (браузеру).
     */
    private void pushStudentDesktopNotification() {
        FacesUtils.push("/notify", createStudentDesktopNotification());
    }

    public void setSelectedStudent(LessonStudentModel model) {
        if (model == null) {
            return;
        }
        selectedStudent = model;
        stream = this.selectedLesson.getStream();

        List<Lesson> lessons = new ArrayList<>();
        Set<Student> studentSet = new HashSet<>();
        if (stream != null && selectedLesson != null) {
            if (selectedLesson.getGroup() != null) {
                studentSet.addAll(selectedLesson.getGroup().getStudents());
                lessons = stream.getLessons().stream().filter(l -> l.getGroup() == null || (selectedLesson.getGroup().equals(l.getGroup()))).collect(Collectors.toList());
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
        List<LessonStudentModel> additionalStudents = StudentDAO.getAdditionalStudents(selectedLesson.getId()).stream()
            .map(s -> new LessonStudentModel(s, stream, true)).collect(Collectors.toList());

        students = studentSet.stream().map(s -> new LessonStudentModel(s, stream)).collect(Collectors.toList());
        students.addAll(additionalStudents);
        students = students.stream().sorted(Comparator.comparing(LessonStudentModel::getName)).collect(Collectors.toList());

        Map<Integer, Map<String, Integer>> skipInfo = StudentDAO.getSkipInfo(stream.getId(), selectedLesson.getId());
        students.stream().forEach(s -> {
            if (skipInfo.containsKey(s.getId())) {
                s.setTotalSkip(skipInfo.get(s.getId()).get(Constants.TOTAL_SKIP));
            }
        });

        selectedStudentLazyModel = new LazyStudentDataModel(students);
    }

    public void onCellEdit(CellEditEvent event) throws Exception {
        if (!event.getColumn().getColumnKey().contains("attestation")) {
            Integer id = lessons.get(((DynamicColumn) event.getColumn()).getIndex()).getId();
            EntityDAO.update(selectedStudent.getStudent().getStudentLessons().get(id));
        } else {

        }
    }

    public void clearStudentLessonInfoDialog() {
        lessons = null;
        attestations = null;
        exams = null;

        students = null;
        selectedStudent = null;

        selectedStudentLazyModel = null;
    }

    public void removeNotification(StudentNotification notification) {
        EntityDAO.delete(notification);
        selectedStudent.getStudent().getNotifications().remove(notification);
    }

    public void saveNotification() {
        if (newNotification != null && !newNotification.isEmpty()) {
            StudentNotification studentNotification = new StudentNotification();
            studentNotification.setActive(Boolean.TRUE);
            studentNotification.setDescription(newNotification);
            studentNotification.setStudent(selectedStudent.getStudent());
            studentNotification.setCreateDate(LocalDateTime.now());
            selectedStudent.getStudent().getNotifications().add(studentNotification);
        }
        newNotification = null;
        EntityDAO.save(selectedStudent.getStudent().getNotifications());
        FacesUtils.closeDialog("notificationDialog");
    }

    private void checkStudentNotifications(Student student) {
        if (notificationSettingsBean.isActive()) {

            NotificationSetting studentNotificationSettings = notificationSettingsBean.getSettings().get(NotificationType.STUDENT.name());
            if (studentNotificationSettings != null && studentNotificationSettings.getActive()) {
                if (student.getNotifications().parallelStream().anyMatch(StudentNotification::getActive)) {
                    notificationSettingsBean.play(studentNotificationSettings);
                    return;
                }
            }

            NotificationSetting absenceNotificationSettings = notificationSettingsBean.getSettings().get(NotificationType.ABSENCE.name());
            if (absenceNotificationSettings != null && absenceNotificationSettings.getActive()) {
                if (skipInfo.containsKey(student.getId())) {
                    Integer totalSkip = skipInfo.get(student.getId()).get(Constants.TOTAL_SKIP) - StudentDAO.getStudentAdditionalLessonsAmount(student.getId(), disciplineId);
                    if (totalSkip >= absenceNotificationSettings.getData()) {
                        notificationSettingsBean.play(absenceNotificationSettings);
                        return;
                    }
                }
            }

            NotificationSetting praepostorNotificationSettings = notificationSettingsBean.getSettings().get(NotificationType.PRAEPOSTOR.name());
            if (praepostorNotificationSettings != null && praepostorNotificationSettings.getActive()) {
                if (selectedLesson.getGroup() == null) {
                    if (selectedLesson.getStream().getGroups().parallelStream().anyMatch(g -> student.getPraepostorGroups().contains(g))) {
                        notificationSettingsBean.play(praepostorNotificationSettings);
                    }
                } else {
                    if (student.getPraepostorGroups().contains(selectedLesson.getGroup())) {
                        notificationSettingsBean.play(praepostorNotificationSettings);
                    }
                }
            }

        }
    }

    public void removeNote(Note note) {
        EntityDAO.delete(note);
        selectedStudent.getStudent().getStudentLessons().get(selectedLesson.getId()).getNotes().remove(note);
    }


    public void saveNote() {
        if (newNote != null && !newNote.isEmpty()) {
            Note note = new Note();
            note.setCreateDate(LocalDateTime.now());
            note.setDescription(newNote);
            note.setType(Constants.STUDENT_LESSON);
            note.setEntityId(selectedStudent.getStudent().getStudentLessons().get(selectedLesson.getId()).getId());
            selectedStudent.getStudent().getStudentLessons().get(selectedLesson.getId()).getNotes().add(note);
            EntityDAO.save(note);
        }
        newNote = null;
        FacesUtils.closeDialog("studentNotesDialog");
    }


    public void removeLessonNote(Note note) {
        EntityDAO.delete(note);
        selectedLesson.getNotes().remove(note);
    }


    public void saveLessonNote() {
        if (newLessonNote != null && !newLessonNote.isEmpty()) {
            Note note = new Note();
            note.setCreateDate(LocalDateTime.now());
            note.setDescription(newLessonNote);
            note.setType(Constants.LESSON);
            note.setEntityId(selectedLesson.getId());
            selectedLesson.getNotes().add(note);
            EntityDAO.save(note);
        }
        newLessonNote = null;
        FacesUtils.closeDialog("lessonNotesDialog");
    }

    private String convertLessonType(LessonType lessonType) {
        LocaleUtils localeUtils = new LocaleUtils(localeBean.getLocale());
        switch (lessonType) {
            case LECTURE: {
                return localeUtils.getMessage("lecture");
            }
            case PRACTICAL: {
                return localeUtils.getMessage("practical");
            }
            case LAB: {
                return localeUtils.getMessage("lab");
            }
        }
        return "";
    }

    private List<?> removeElements(List<?> list, long count) {
        if (list.size() <= count) {
            return new ArrayList<>();
        }

        for (long i = count; i > 0; i--) {
            list.remove(0);
        }

        return list;
    }
}
