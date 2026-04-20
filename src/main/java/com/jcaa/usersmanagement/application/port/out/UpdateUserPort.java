package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserModel;

public interface UpdateUserPort {
  static UserModel update(UserModel user);

    UserModel update(UserModel user);
}
