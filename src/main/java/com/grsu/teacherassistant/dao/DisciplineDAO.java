package com.grsu.teacherassistant.dao;

import com.grsu.teacherassistant.entities.Student;
import com.grsu.teacherassistant.utils.db.DBSessionFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisciplineDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentDAO.class);

    public static Integer getDisciplineByLessonId(Integer lessonId){
        Session session = DBSessionFactory.getSession();
        try {
            Query query = session.createNativeQuery("select d.id from Lesson l " +
                "join Stream str on l.stream_id = str.id " +
                "join Discipline d on str.discipline_id = d.id " +
                "where l.id = :lessonId");
            query.setParameter("lessonId", lessonId);
            query.setFirstResult(0);
            query.setMaxResults(1);
            return (Integer) query.uniqueResult();
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
        }
        return null;
    }
}
