package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.upgrad.quora.service.business.SignupBusinessService;


import java.util.UUID;
import java.util.Base64;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserBusinessService userBusinessService;

    @Autowired
    private AuthenticationService authenticationService;


    @RequestMapping(method = RequestMethod.POST, path = "/users/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> signup(final SignupUserRequest signupUserRequest)  {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUserName(signupUserRequest.getUserName());
        userEntity.setEmailAddress(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutMe(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());
        userEntity.setSalt("1234abc");
        userEntity.setRole("nonadmin");
        try {
            final UserEntity createdUserEntity = userBusinessService.createUser(userEntity);
            SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
            return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
        }catch(SignUpRestrictedException signupRE){
            ErrorResponse errorResponse = new ErrorResponse().message(signupRE.getErrorMessage()).code(signupRE.getCode()).rootCause(signupRE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);

        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/users/signin" , produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> signin(@RequestHeader("authorization") final String authorization ) throws AuthenticationFailedException {
        byte[] decode = Base64.getDecoder().decode(authorization);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");
        UserAuthEntity userAuthToken;
        try {
            userAuthToken = authenticationService.authenticate(decodedArray[0], decodedArray[1]);
        }catch(AuthenticationFailedException afe){
            ErrorResponse errorResponse = new ErrorResponse().message(afe.getErrorMessage()).code(afe.getCode()).rootCause(afe.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        UserEntity user = userAuthToken.getUser();
        SigninResponse signinResponse = new SigninResponse().id(user.getUuid())
                .message("SIGNED IN SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthToken.getAccessToken());
        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/users/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> signout(@RequestHeader("authorization") final String accesToken ) throws SignOutRestrictedException {
        UserAuthEntity userAuthToken;
        try {
            userAuthToken = userBusinessService.signout(accesToken);
            UserEntity user = userAuthToken.getUser();
        }catch(SignOutRestrictedException signOutRE){
            ErrorResponse errorResponse = new ErrorResponse().message(signOutRE.getErrorMessage()).code(signOutRE.getCode()).rootCause(signOutRE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
        UserEntity user = userAuthToken.getUser();
        SignoutResponse signoutResponse = new SignoutResponse().id(user.getUuid())
                .message("SIGNED OUT SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthToken.getAccessToken());
        return new ResponseEntity<SignoutResponse>(signoutResponse, headers, HttpStatus.OK);
    }
}
