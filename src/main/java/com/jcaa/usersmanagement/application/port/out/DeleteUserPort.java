package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.valueobject.UserId;

public interface DeleteUserPort {
    static void delete(UserId userId) {

    }

    void delete(UserId userId);
}
