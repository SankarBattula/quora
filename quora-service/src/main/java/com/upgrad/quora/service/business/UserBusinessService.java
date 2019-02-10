package com.upgrad.quora.service.business;

import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(final UserEntity userEntity) throws SignUpRestrictedException {

        // Check for username exist ...
        final String username = userEntity.getUserName();
        final UserEntity fetchedUserByName =  userDao.getUserByUserName(username);
        if (fetchedUserByName != null) {
            throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");
        }
        // Check for email exist ...
        final String email = userEntity.getEmailAddress();
        final UserEntity fetchedUserByEmail = userDao.getUserByEmail(email);
        if(fetchedUserByEmail != null) {
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);

    }
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signout(final String accessToken) throws SignOutRestrictedException {
        UserAuthEntity userAuthEntity = userDao.getAuthByAccessToken(accessToken);
        if (userAuthEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }
        userAuthEntity.setLogoutAt(ZonedDateTime.now());
        return userDao.signOut(userAuthEntity);
    }
    public UserEntity getUser(final String userUuid){
        return userDao.getUser(userUuid);
    }
    public UserEntity userProfile(final String userUuid,final String accessToken) throws AuthenticationFailedException,UserNotFoundException{
        UserAuthEntity userAuthEntity = userDao.getAuthByAccessToken(accessToken);
        UserEntity userEntity =  userDao.getUser(userUuid);
        if (userAuthEntity == null) {
            throw new AuthenticationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthenticationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
        }
        if(userEntity == null){
            throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
        }else{
            return userEntity;
        }
    }

    public UserAuthEntity deleteUser(final String userUuid,final String accessToken) throws AuthenticationFailedException{
        UserAuthEntity userAuthEntity = userDao.getAuthByAccessToken(accessToken);
        UserEntity userEntity =  userDao.getUser(userUuid);
        if (userAuthEntity == null) {
                throw new AuthenticationFailedException("ATHR-001", "User has not signed in");
         }
        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthenticationFailedException("ATHR-002", "User is signed out");
        }
        if(userEntity == null){
            throw new AuthenticationFailedException("USR-001", "User with entered uuid to be deleted does not exist");
        }else{
            if (userEntity.getRole().equalsIgnoreCase("nonadmin")) {
                throw new AuthenticationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
            }
            userDao.deleteUser(userEntity,userAuthEntity);
            return userAuthEntity;
        }
    }
}
