package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.ErrorResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.SignupBusinessService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class CommonController {

    @Autowired
    private UserBusinessService userBusinessService;

    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> userProfile(@PathVariable String userId,@RequestHeader("authorization") final String accessToken) throws AuthenticationFailedException,UserNotFoundException {
        try {
            final UserEntity user = userBusinessService.userProfile(userId,accessToken);
            UserDetailsResponse userResponse = new UserDetailsResponse().firstName(user.getFirstName()).lastName(user.getLastName()).userName(user.getUserName())
                    .emailAddress(user.getEmailAddress()).country(user.getCountry()).aboutMe(user.getAboutMe()).dob(user.getDob()).contactNumber(user.getContactNumber());
            return new ResponseEntity<UserDetailsResponse>(userResponse, HttpStatus.OK);
        }catch(AuthenticationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            if(authFE.getCode().equalsIgnoreCase("USR-001"))
                return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }catch(UserNotFoundException userNFE){
            ErrorResponse errorResponse = new ErrorResponse().message(userNFE.getErrorMessage()).code(userNFE.getCode()).rootCause(userNFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

}
