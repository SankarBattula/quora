package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserBusinessService userBusinessService;

    private UserEntity getUserByAccessToken(final String accessToken, final String errorMsg) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userBusinessService.getUserByAccessToken(accessToken);
        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", errorMsg);
        }
        return userAuthEntity.getUser();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        UserEntity user = getUserByAccessToken(authorization, "User is signed out.Sign in first to post a question.");

        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(new Date());
        questionEntity.setUser(user);

        final QuestionEntity createdQuestion = questionService.createQuestion(questionEntity);

        final QuestionResponse questionResponse = new QuestionResponse().id(createdQuestion.getUuid()).status("QUESTION CREATED");

        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<QuestionDetailsResponse> getAllQuestions(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        UserEntity user = getUserByAccessToken(authorization, "User is signed out.Sign in first to get all questions.");

        List<QuestionDetailsResponse> responseList = new ArrayList<>();

        List<QuestionEntity> questionList = questionService.getAllQuestions();
        for (QuestionEntity question: questionList) {
            responseList.add(new QuestionDetailsResponse().id(question.getUuid()).content(question.getContent()));
        }

        return responseList;
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestion(final QuestionEditRequest editRequest, @PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserEntity user = getUserByAccessToken(authorization, "User is signed out.Sign in first to edit the question.");

        QuestionEntity questionEntity = questionService.editQuestion(questionId, editRequest.getContent());

        if (user != questionEntity.getUser()) {
            throw new AuthorizationFailedException("ATHR-003", "Only the owner can edit the question.");
        }

        final QuestionEditResponse editResponse = new QuestionEditResponse().id(questionEntity.getUuid()).status("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(editResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserEntity user = getUserByAccessToken(authorization, "User is signed out.Sign in first to delete a question.");

        QuestionEntity questionEntity = questionService.deleteQuestion(questionId);

        if (user != questionEntity.getUser() && user.getRole().equals("nonadmin")) {
            throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question.");
        }

        final QuestionDeleteResponse deleteResponse = new QuestionDeleteResponse().id(questionEntity.getUuid()).status("QUESTION DELETED");

        return new ResponseEntity<QuestionDeleteResponse>(deleteResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "question/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<QuestionDetailsResponse> getAllQuestionsByUser(@PathVariable("userId") final String userId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        UserEntity user = getUserByAccessToken(authorization, "User is signed out.Sign in first to get all questions posted by a specific user.");

        user = userBusinessService.getUser(userId);

        List<QuestionDetailsResponse> responseList = new ArrayList<>();

        List<QuestionEntity> questionList = questionService.getAllQuestionsByUser(user);
        for (QuestionEntity question: questionList) {
            responseList.add(new QuestionDetailsResponse().id(question.getUuid()).content(question.getContent()));
        }

        return responseList;
    }
}
