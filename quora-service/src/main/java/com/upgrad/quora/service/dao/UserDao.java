package com.upgrad.quora.service.dao;


import com.upgrad.quora.service.entity.UserAuthEntity;
import org.springframework.stereotype.Repository;
import com.upgrad.quora.service.entity.UserEntity;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

    @Repository
    public class UserDao {

        @PersistenceContext
        private EntityManager entityManager;

        public UserEntity createUser(UserEntity userEntity) {
            entityManager.persist(userEntity);
            return userEntity;
        }

        public UserEntity getUserByEmail(final String email) {
            try {
                return entityManager.createNamedQuery("userByEmail", UserEntity.class).setParameter("email", email).getSingleResult();
            } catch (Exception nre) {
                return null;
            }
        }

        public UserAuthEntity createAuthToken(final UserAuthEntity userAuthEntity) {
            entityManager.persist(userAuthEntity);
            return userAuthEntity;
        }

        public UserEntity getUser(String userUuid){
            try {
                return entityManager.createNamedQuery("userByUuid", UserEntity.class).setParameter("uuid", userUuid)
                        .getSingleResult();
            }catch(Exception nre){
                return null;
            }
        }

        public UserEntity deleteUser(String userUuid){
            try {
                return entityManager.createNamedQuery("deleteByUuid", UserEntity.class).setParameter("uuid", userUuid)
                        .getSingleResult();
            }catch(Exception nre){
                return null;
            }
        }

        public UserAuthEntity getUserByAccessToken(final String accessToken) {
            try {
                return entityManager.createNamedQuery("userAuthTokenByAccessToken", UserAuthEntity.class).setParameter("accessToken", accessToken).getSingleResult();
            } catch (NoResultException nre) {
                return null;
            }
        }
    }