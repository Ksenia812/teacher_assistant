package com.grsu.teacherassistant.models;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class StudentSkip {

    private Date skipDate;
    private LessonType lessonType;

    public StudentSkip(Date skipDate, LessonType lessonType) {
        this.skipDate =  skipDate;
        this.lessonType = lessonType;
    }

    public Date getSkipDate() {
        return skipDate;
    }

    public void setSkipDate(Date skipDate) {
        this.skipDate = skipDate;
    }

    public LessonType getLessonType() {
        return lessonType;
    }

    public void setLessonType(LessonType lessonType) {
        this.lessonType = lessonType;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        return String.format("%s %s", formatter.format(skipDate), lessonType);
    }
}
