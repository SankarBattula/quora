package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.type.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Controller class for defining answer related operations.
 */
@RestController
@RequestMapping("/")
public class AnswerController {

    // Autowired answer service from business
    @Autowired
    AnswerService answerService;

    // Autowired question service from business
    @Autowired
    QuestionService questionService;

    // Autowired user business service
    @Autowired
    UserBusinessService userBusinessService;


    /**
     * @param answerRequest
     * @param questionUuId
     * @param authorization
     * @return answer response with the status created
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     *
     * Method is used to create answer to answer with respect to question id
     */

    // This controller method is called when the request pattern is of type
    // 'createAnswer' and also the incoming request is of POST Type
    // The method calls the createAnswer() method in the business logic
    // Looks for a controller method with mapping of type
    // '/question/{questionId}answer/create'
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> createAnswer(final AnswerRequest answerRequest, @PathVariable("questionId") final String questionUuId, @RequestHeader final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        //Authorize the user
        UserAuthEntity authorizedUser;
        try {
            authorizedUser = userBusinessService.getUserByAccessToken(authorization);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }
        //get the question object from database
        Question question ;
        try {
            question = questionService.getQuestionForUuId(questionUuId);
        }catch(InvalidQuestionException iQE){
            ErrorResponse errorResponse = new ErrorResponse().message(iQE.getErrorMessage()).code(iQE.getCode()).rootCause(iQE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }

        //Create answer object
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswer(answerRequest.getAnswer());
        answer.setUuid(UUID.randomUUID().toString());
        answer.setUser(authorizedUser.getUser());
        ZonedDateTime now = ZonedDateTime.now();
        answer.setDate(now);
        //Send the answer object from creation in database
        Answer createdAnswer = answerService.createAnswer(answer);
        //create answer reponse object
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswer.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }


    /**
     * @param answerEditRequest
     * @param answerUuId
     * @param authorization
     * @return edited response with status ok
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     *
     * Method used edit answer with respect to answer id
     */
    // This controller method is called when the request pattern is of type
    // 'editAnswerContent' and also the incoming request is of PUT Type
    // The method calls the editAnswer() method in the business logic passing the
    // answer to be update
    // Looks for a controller method with mapping of type
    // '/answer/edit/{answerId}'
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> editAnswerContent(AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        //Authorize the user if he has signed in properly
        UserAuthEntity authorizedUser;
        try {
            authorizedUser = userBusinessService.getUserByAccessToken(authorization);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }

        //get the answer Object after checking if user if owner of the answer
        Answer answer;
        try {
            answer = answerService.isUserAnswerOwner(answerUuId, authorizedUser, ActionType.EDIT_ANSWER);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }catch(AnswerNotFoundException aNFE){
            ErrorResponse errorResponse = new ErrorResponse().message(aNFE.getErrorMessage()).code(aNFE.getCode()).rootCause(aNFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }
        //set the details that needs to updated in database
        answer.setAnswer(answerEditRequest.getContent());
        answer.setDate(ZonedDateTime.now());
        Answer editedAnswer = answerService.editAnswer(answer);
        AnswerEditResponse answerEditResponse = new AnswerEditResponse()
                .id(answerUuId)
                .status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }


    /**
     *
     * @param answerUuId
     * @param authorization
     * @return With the status ok
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     *
     * Method is used to delete particular answer for an question with respect to answer id
     */
    // This controller method is called when the request pattern is of type
    // 'deleteAnswer' and also the incoming request is of DELETE Type
    // The method calls the deleteAnswer() method in the business logic passing the
    // answer to be deleted
    // Looks for a controller method with mapping of type
    // '/answer/delete/{answerId}'
    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> deleteAnswer(@PathVariable("answerId") final String answerUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        //Authorize the user if he has signed in properly
        UserAuthEntity authorizedUser;
        try {
            authorizedUser = userBusinessService.getUserByAccessToken(authorization);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }
        //Check if the user is himself or an admin trying to delete the answer
        Answer answer;
        try {
            answer = answerService.isUserAnswerOwner(answerUuId, authorizedUser, ActionType.DELETE_ANSWER);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }catch(AnswerNotFoundException aNFE){
            ErrorResponse errorResponse = new ErrorResponse().message(aNFE.getErrorMessage()).code(aNFE.getCode()).rootCause(aNFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }
        answerService.deleteAnswer(answer);
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse()
                .id(answerUuId)
                .status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }

    /***
     *
     * @param questionId
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     * @throws AnswerNotFoundException
     *
     * Method is used to get all answer to particular question with respect to question id
     */
    // This controller method is called when the request pattern is of type
    // 'getAllAnswersToQuestion' and also the incoming request is of GET Type
    // Looks for a controller method with mapping of type
    // '/answer/all/{questionId}'
    @RequestMapping(method = RequestMethod.GET, path = "/answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAllAnswersToQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException, AnswerNotFoundException {
        //Authorize the user if he has signed in properly
        UserAuthEntity authorizedUser;
        try {
            authorizedUser = userBusinessService.getUserByAccessToken(authorization);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }
        List<Answer> answerList;
        try {
            answerList = answerService.getAnswersForQuestion(questionId);
        }catch(InvalidQuestionException iQE) {
            ErrorResponse errorResponse = new ErrorResponse().message(iQE.getErrorMessage()).code(iQE.getCode()).rootCause(iQE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }catch(AnswerNotFoundException aNFE){
            ErrorResponse errorResponse = new ErrorResponse().message(aNFE.getErrorMessage()).code(aNFE.getCode()).rootCause(aNFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }
        StringBuilder contentBuilder = new StringBuilder();
        getContentsString(answerList, contentBuilder);
        StringBuilder uuIdBuilder = new StringBuilder();
        String questionContentValue = getUuIdStringAndQuestionContent(answerList, uuIdBuilder);
        AnswerDetailsResponse response = new AnswerDetailsResponse()
                .id(uuIdBuilder.toString())
                .answerContent(contentBuilder.toString())
                .questionContent(questionContentValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * private utility method for appending the uuid of answers.
     *
     * @param answerList  List of questions
     * @param uuIdBuilder StringBuilder object
     */
    public static final String getUuIdStringAndQuestionContent(List<Answer> answerList, StringBuilder uuIdBuilder) {
        String questionContent = new String();
        for (Answer answerObject : answerList) {
            uuIdBuilder.append(answerObject.getUuid()).append(",");
            questionContent = answerObject.getQuestion().getContent();
        }
        return questionContent;
    }

    /**
     * private utility method for providing contents string in appended format
     *
     * @param answerList list of questions
     * @param builder    StringBuilder with appended content list.
     */
    public static final StringBuilder getContentsString(List<Answer> answerList, StringBuilder builder) {
        for (Answer answerObject : answerList) {
            builder.append(answerObject.getAnswer()).append(",");
        }
        return builder;
    }

}
