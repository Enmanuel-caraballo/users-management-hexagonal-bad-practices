package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserId;


import java.util.Optional;

public interface GetUserByIdPort {
    static Optional<UserModel> getById(UserId userId) {
        return null;
    }

    Optional<UserModel> getById(UserId userId);
}
