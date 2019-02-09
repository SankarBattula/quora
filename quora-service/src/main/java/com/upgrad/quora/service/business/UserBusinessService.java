package com.upgrad.quora.service.business;

import com.upgrad.quora.service.exception.AuthenticationFailedException;
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

    public UserEntity getUser(final String userUuid){
        return userDao.getUser(userUuid);
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
