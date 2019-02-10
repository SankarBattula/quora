package com.upgrad.quora.service.business;

import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    public UserAuthEntity getUserByAccessToken(final String accessToken) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserByAccessToken(accessToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in.");
        }
        return userAuthEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(final UserEntity userEntity) {

        String password = userEntity.getPassword();
        if (password == null) {
            userEntity.setPassword("proman@123");
        }
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);

    }

    public UserEntity getUser(final String userUuid) throws AuthorizationFailedException {
        UserEntity user = userDao.getUser(userUuid);
        if (user == null) {
            throw new AuthorizationFailedException("USR-001", "User with entered uuid whose question details are to be seen does not exist.");
        }
        return user;
    }
    public UserEntity deleteUser(final String userUuid) throws AuthenticationFailedException{
        UserEntity userEntity =  userDao.getUser(userUuid);
        // String userRole = userEntity.getRole();
        if (userEntity.getUuid() == null) {
            throw new AuthenticationFailedException("ATH-001", "User has not signed in");
        }
        /*if (userEntity.getUuid() == "signout") {
            throw new AuthenticationFailedException("ATHR-002", "User is signed out");
        }*/
        if (userEntity.getRole().equalsIgnoreCase("nonadmin")) {
            throw new AuthenticationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }
        if(userEntity == null){
            throw new AuthenticationFailedException("USR-001", "User with entered uuid to be deleted does not exist");
        }else{
            return userDao.deleteUser(userUuid);
        }


    }
}
