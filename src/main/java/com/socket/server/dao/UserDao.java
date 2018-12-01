package com.socket.server.dao;

import com.socket.server.entity.AppUser;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // TODO
    private List<String> fetchUnreadMessage() {
        return new ArrayList<>();
    }

    public AppUser validate(String username, String password) {
        if (isValid(username, password)) {
            return getUser(username, password);
        } else {
            return null;
        }
    }

    // TODO
    private boolean isValid(String username, String password) {
        return true;
    }

    private AppUser getUser(String username, String password) {
        AppUser user = new AppUser(username, password);
        user.setUnreadMessages(fetchUnreadMessage());
        return user;
    }
}
