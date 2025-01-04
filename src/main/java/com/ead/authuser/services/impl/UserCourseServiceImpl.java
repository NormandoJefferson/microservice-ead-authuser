package com.ead.authuser.services.impl;

import com.ead.authuser.repositories.UserCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCourseServiceImpl {

    @Autowired
    UserCourseRepository userCourseRepository;

}