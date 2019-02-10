package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.ErrorResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AdminController {

    @Autowired
    private UserBusinessService userBusinessService;

    @RequestMapping(method = RequestMethod.DELETE, path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> userDelete(@PathVariable String userId,
                                                         @RequestHeader("authorization") final String accessToken) throws AuthenticationFailedException,UserNotFoundException {
        UserAuthEntity userAuthToken;
        try {
            userAuthToken = userBusinessService.deleteUser(userId,accessToken);
            UserEntity user = userAuthToken.getUser();
        }catch(AuthenticationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }catch(UserNotFoundException userNFE){
            ErrorResponse errorResponse = new ErrorResponse().message(userNFE.getErrorMessage()).code(userNFE.getCode()).rootCause(userNFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }
        UserEntity user = userAuthToken.getUser();
        SignoutResponse signoutResponse = new SignoutResponse().id(user.getUuid())
                .message("USER SUCCESSFULLY DELETED");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthToken.getAccessToken());
        return new ResponseEntity<SignoutResponse>(signoutResponse, headers, HttpStatus.OK);
    }
}