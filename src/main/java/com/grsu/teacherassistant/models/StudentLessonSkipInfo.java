package com.grsu.teacherassistant.models;

import java.util.Date;

/**
 * @author Pavel Zaychick
 */
public class StudentLessonSkipInfo {
	private Integer studentId;
	private LessonType lessonType;
	private Date date;

	public StudentLessonSkipInfo(Integer studentId, Integer lessonType, Date date) {
		this.studentId = studentId;
		this.lessonType = LessonType.getLessonTypeByCode(lessonType);
		this.date = date;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}

	public LessonType getLessonType() {
		return lessonType;
	}

	public void setLessonType(LessonType lessonType) {
		this.lessonType = lessonType;
	}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
