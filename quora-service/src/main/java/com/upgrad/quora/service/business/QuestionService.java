package com.upgrad.quora.service.business;

import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.dao.QuestionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    @Autowired
    private QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        return questionDao.createQuestion(questionEntity);
    }
}
