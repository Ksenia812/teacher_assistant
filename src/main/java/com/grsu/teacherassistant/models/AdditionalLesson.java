package com.grsu.teacherassistant.models;

import java.util.Date;

public class AdditionalLesson {

    private Date date;
    private LessonType lessonType;
    private String groupName;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LessonType getLessonType() {
        return lessonType;
    }

    public void setLessonType(LessonType lessonType) {
        this.lessonType = lessonType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public AdditionalLesson(Date date, LessonType lessonType, String groupName) {
        this.date = date;
        this.lessonType = lessonType;
        this.groupName = groupName;
    }
}
