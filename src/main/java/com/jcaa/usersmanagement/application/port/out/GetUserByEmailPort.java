package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import java.util.Optional;

public interface GetUserByEmailPort {
    static Optional<UserModel> getByEmail(UserEmail email) {
        return null;
    }

    Optional<UserModel> getByEmail(UserEmail email);
}
