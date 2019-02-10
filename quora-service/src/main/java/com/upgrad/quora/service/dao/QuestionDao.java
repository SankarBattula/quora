package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class QuestionDao {

    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public List<QuestionEntity> getAllQuestions() {
        TypedQuery<QuestionEntity> typedQuery = entityManager.createQuery("SELECT q from QuestionEntity q", QuestionEntity.class);
        List<QuestionEntity> resultList = typedQuery.getResultList();
        return resultList;
    }

    public QuestionEntity getQuestionById(final String questionId) {
        try {
            return entityManager.createNamedQuery("getQuestionByUuid", QuestionEntity.class).setParameter("uuid", questionId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void editQuestion(QuestionEntity questionEntity) {
        entityManager.merge(questionEntity);
    }

    public void deleteQuestion(QuestionEntity questionEntity) {
        entityManager.remove(questionEntity);
    }

    public List<QuestionEntity> getAllQuestionByUser(UserEntity user) {
        TypedQuery<QuestionEntity> typedQuery = entityManager.createQuery("SELECT q from QuestionEntity q WHERE q.user = :user", QuestionEntity.class).setParameter("user", user);
        List<QuestionEntity> resultList = typedQuery.getResultList();
        return resultList;
    }
}
