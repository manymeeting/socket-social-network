package com.socket.server;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public AppUser validate(String readbuf) {
        System.out.println("UserDao.isValid(), readbuf = " + readbuf);
        if(readbuf != null){
            String[] strings = readbuf.split(" ");
            if(strings.length == 2){
                AppUser user = new AppUser(strings[0], strings[1]); 
                user.setUnreadMessages(fetchUnreadMessage());
                return user;
            }
        }
        return null;
    }

    private List<String> fetchUnreadMessage() {
        return new ArrayList<>();
    }

    public void turnOffline(AppUser user) {
        user.setStatus(AppUser.Status.OFFLINE);
    }
}
