package com.grsu.teacherassistant.dao;

import com.grsu.teacherassistant.utils.db.DBSessionFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentLessonDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentLessonDAO.class);

    public static int getStudentAdditionalLessonsAmount(int studentId){
        int studentAdditionalLessonsAmount = 0;
        Session session = DBSessionFactory.getSession();
        try {
            Query query = session.createNamedQuery("StudentAdditionalLessons");
            query.setParameter("studentId", studentId);
            studentAdditionalLessonsAmount = (int) query.getSingleResult();
        } catch (PersistenceException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
        }

        return studentAdditionalLessonsAmount;
    }

    public static List<String> getStudentAdditionalLessonsInfo(int studentId){
        List<String> studentAdditionalLessonsAmount = new ArrayList<>();
        Session session = DBSessionFactory.getSession();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            Query<Object[]> query = session.createNamedQuery("StudentAdditionalLessonsInfo");
            query.setParameter("studentId", studentId);
            for (Object[] object : query.getResultList()) {
                String date = formatter.format(simpleDateFormat.parse(object[1].toString()));
                studentAdditionalLessonsAmount.add(date + " " + object[0]);
            }
        } catch (PersistenceException | ParseException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
        }

        return studentAdditionalLessonsAmount;
    }
}
