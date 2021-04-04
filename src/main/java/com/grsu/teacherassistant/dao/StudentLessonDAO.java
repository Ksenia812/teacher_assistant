package com.grsu.teacherassistant.dao;

import com.grsu.teacherassistant.entities.StudentLesson;
import com.grsu.teacherassistant.models.SkipInfo;
import com.grsu.teacherassistant.utils.db.DBSessionFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.List;

public class StudentLessonDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentLessonDAO.class);

    public static int getStudentAdditionalLessonsAmount(int studentId){
        int studentAdditionalLessonsAmount = 0;
        Session session = DBSessionFactory.getSession();
        try {
            Query query = session.createNamedQuery("StudentAdditionalLessons");
            query.setParameter("studentId", studentId);
            studentAdditionalLessonsAmount = query.getFirstResult();
        } catch (PersistenceException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
        }

        return studentAdditionalLessonsAmount;
    }
}
