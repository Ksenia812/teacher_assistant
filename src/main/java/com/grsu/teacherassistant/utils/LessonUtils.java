package com.grsu.teacherassistant.utils;

import com.grsu.teacherassistant.dao.LessonDAO;
import com.grsu.teacherassistant.entities.Lesson;
import com.grsu.teacherassistant.entities.Note;
import com.grsu.teacherassistant.models.LessonType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This temporary (hah) class is used for decreasing of copy-paste code
 */
public class LessonUtils {

    public static List<Note> loadLastLectureNotes(Lesson currentLesson, boolean forceLoading) {
        return getLastNotesOf(getLastLesson(currentLesson, LessonType.LECTURE, forceLoading));
    }

    public static Lesson getLastLesson(Lesson currentLesson, LessonType lessonType, boolean forceLoading) {
        if (!forceLoading) {
            return null;
        }
        List<Lesson> lessons = LessonDAO.getAll(null, currentLesson.getDate(), true, currentLesson.getStream().getId(), true, null, null, null, lessonType);
        if (lessons == null) {
            return null;
        }
        List<Lesson> result = lessons.stream().filter(lesson ->
            lesson.getDate().isBefore(currentLesson.getDate()) || (
                lesson.getDate().equals(currentLesson.getDate())
                    && lesson.getSchedule().getBegin().isBefore(currentLesson.getSchedule().getBegin())
            )
        ).sorted(((o1, o2) -> o1.getDate().isAfter(o2.getDate()) ? -1 : o1.getDate().equals(o2.getDate()) ? 0 : 1))
            .collect(Collectors.toList());
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private static List<Note> getLastNotesOf(Lesson lesson) {
        if (lesson != null) {
            List<Note> notes = lesson.getNotes();
            notes.sort(Comparator.comparing(Note::getCreateDate).reversed());
            return notes;
        }
        return new ArrayList<>();
    }

    public static List<Note> loadLastPracticeNotes(Lesson currentLesson, boolean forceLoading) {
        return getLastNotesOf(getLastLesson(currentLesson, LessonType.PRACTICAL, forceLoading));
    }
}
