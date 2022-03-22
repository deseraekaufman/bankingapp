package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserDao {

    List<User> findAll(int id);

    User findByUsername(String username);

    int findIdByUsername(String username);

    Double getUserBalance(int id);

    boolean create(String username, String password);

//    int findUsernameByAccountId(int accountId);
}
