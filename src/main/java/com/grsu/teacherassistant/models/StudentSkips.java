package com.grsu.teacherassistant.models;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class StudentSkips {

    private List<Date> skipDate;
    private List<LessonType> lessonType;

    public StudentSkips(List<Date> skipDate, List<LessonType> lessonType) {
        this.skipDate =  skipDate;
        this.lessonType = lessonType;
    }

    public List<Date> getSkipDate() {
        return skipDate;
    }

    public void setSkipDate(List<Date> skipDate) {
        this.skipDate = skipDate;
    }

    public List<LessonType> getLessonType() {
        return lessonType;
    }

    public void setLessonType(List<LessonType> lessonType) {
        this.lessonType = lessonType;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        for(Date d : skipDate){
            stringBuilder.append(formatter.format(d));
            stringBuilder.append("\n");
        }
        for (LessonType lesson: lessonType) {
            stringBuilder.append(lesson.getKey());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
