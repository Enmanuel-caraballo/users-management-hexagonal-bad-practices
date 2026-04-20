package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserModel;

public interface SaveUserPort {
    static UserModel save(UserModel user) {
        return null;
    }


  UserModel save(UserModel user);
}
