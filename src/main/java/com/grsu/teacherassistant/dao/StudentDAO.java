package com.grsu.teacherassistant.dao;

import com.grsu.teacherassistant.constants.Constants;
import com.grsu.teacherassistant.entities.Student;
import com.grsu.teacherassistant.models.AdditionalLesson;
import com.grsu.teacherassistant.models.LessonType;
import com.grsu.teacherassistant.models.SkipInfo;
import com.grsu.teacherassistant.models.StudentSkip;
import com.grsu.teacherassistant.utils.db.DBSessionFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StudentDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentDAO.class);

    public static List<Student> getAll() {
        return getAll(false);
    }

    public static List<Student> getAll(boolean includeArchived) {
        try (Session session = DBSessionFactory.getSession()) {
            LOGGER.info("Start loading Students from database.");
            String queryString = "" +
                "select distinct s " +
                "from Student s " +
                "   left join fetch s.groups g %s" +
                "order by s.lastName, s.firstName";
            if (!includeArchived) {
                queryString = String.format(queryString,
                    "where (g.active = true and (g.expirationDate > current_date or g.expirationDate is null)) or size (s.groups) = 0 "
                );
            } else {
                queryString = String.format(queryString, "");
            }
            Query query = session.createQuery(queryString);
            return query.getResultList();
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("End loading Students from database.");
        }
        return null;
    }

    public static Student getByCardUid(String cardUid) {
        Session session = DBSessionFactory.getSession();
        try {
            Query query = session.createQuery("from Student where cardUid = :cardUid");
            query.setParameter("cardUid", cardUid);
            query.setFirstResult(0);
            query.setMaxResults(1);
            return (Student) query.uniqueResult();
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
        }
        return null;
    }

    public static Map<Integer, Map<String, Integer>> getSkipInfo(int streamId, int lessonId) {
        List<SkipInfo> skipInfoList = null;
        Session session = DBSessionFactory.getSession();
        try {
            Query query = session.createNamedQuery("SkipInfoQuery", SkipInfo.class);
            query.setParameter("streamId", streamId);
            query.setParameter("lessonId", lessonId);
            skipInfoList = query.getResultList();
        } catch (PersistenceException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
        }

        Map<Integer, Map<String, Integer>> skipInfo = new HashMap<>();
        if (skipInfoList != null) {
            for (SkipInfo si : skipInfoList) {
                if (!skipInfo.containsKey(si.getStudentId())) {
                    Map<String, Integer> studentSkipInfoMap = new HashMap<>();
                    studentSkipInfoMap.put(Constants.TOTAL_SKIP, 0);
                    skipInfo.put(si.getStudentId(), studentSkipInfoMap);
                }
                skipInfo.get(si.getStudentId()).put(si.getLessonType().getKey(), si.getCount());
                int total = skipInfo.get(si.getStudentId()).get(Constants.TOTAL_SKIP);
                skipInfo.get(si.getStudentId()).put(Constants.TOTAL_SKIP, total + si.getCount());
            }
        }
        return skipInfo;
    }

    public static List<SkipInfo> getStudentSkipInfo(List<Integer> studentId, int streamId, int lessonId) {
        final long t = System.currentTimeMillis();
        LOGGER.info("==> getStudentSkipInfo(); studentId = " + studentId + "; streamId = " + streamId + "; lessonId = " + lessonId);
        Session session = DBSessionFactory.getSession();
        try {
            Query query = session.createNamedQuery("StudentSkipInfoQuery", SkipInfo.class);
            query.setParameterList("studentId", studentId);
            query.setParameter("streamId", streamId);
            query.setParameter("lessonId", lessonId);
            return query.getResultList();
        } catch (PersistenceException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
            LOGGER.info("<== getStudentSkipInfo(); " + (System.currentTimeMillis() - t));
        }
        return null;
    }

    //gets student's skips dates and lessons
    public static List<StudentSkip> getStudentSkipsDates(Integer studentId, int lessonId){
        Session session = DBSessionFactory.getSession();
        Query<Object[]> query = session.createNamedQuery("StudentSkipsQuery");
        query.setParameter("studentId", studentId);
        query.setParameter("lessonId", lessonId);

        List<StudentSkip> studentSkips = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for(Object[] result : query.getResultList()){
            try {
                Date date = formatter.parse((String) result[0]);
                LessonType lessonType = LessonType.getLessonTypeByCode(((Integer)result[1]));
                studentSkips.add(new StudentSkip(date, lessonType));
            } catch (ParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return studentSkips;
    }

    public static List<Date> getStudentSkipsByLessonType(Integer studentId, int lessonTypeCode, int lessonId){
        Session session = DBSessionFactory.getSession();
        Query queryDates = session.createNamedQuery("StudentSkipsByLessonTypeQuery");
        queryDates.setParameter("studentId", studentId);
        queryDates.setParameter("lessonTypeId", lessonTypeCode);
        queryDates.setParameter("lessonId", lessonId);

        List<Date> dates = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for(Object date : queryDates.list()){
            try {
                dates.add(formatter.parse((String) date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dates;
    }

    public static List<Student> getAdditionalStudents(int lessonId) {
        Session session = DBSessionFactory.getSession();
        try {
            Query query = session.createNamedQuery("AdditionalStudents", Student.class);
            query.setParameter("lessonId", lessonId);
            return query.getResultList();
        } catch (PersistenceException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
        }
        return null;
    }

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

    public static List<AdditionalLesson> getStudentAdditionalLessonsInfo(int studentId){
        List<AdditionalLesson> additionalLessons = new ArrayList<>();
        Session session = DBSessionFactory.getSession();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Query<Object[]> query = session.createNamedQuery("StudentAdditionalLessonsInfo");
            query.setParameter("studentId", studentId);
            for (Object[] object : query.getResultList()) {
                Date date = formatter.parse((String)object[1]);
                LessonType lessonType = LessonType.getLessonTypeByCode(((Integer)object[0]));
                additionalLessons.add(new AdditionalLesson(date, lessonType, (String)object[2]));
            }
        } catch (PersistenceException | ParseException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
        }

        return additionalLessons;
    }
}
