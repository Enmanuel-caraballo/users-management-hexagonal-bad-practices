package com.jcaa.usersmanagement;

import com.jcaa.usersmanagement.infrastructure.config.DependencyContainer;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.UserManagementCli;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import java.util.Scanner;
import java.util.logging.Logger;

public final class Main {

  private static final Logger log = Logger.getLogger(Main.class.getName());

  public static void main(final String[] args) {
    log.info("Starting Users Management System...");
    final DependencyContainer container = buildContainer();
    buildCli(container).start();
  }

  private static DependencyContainer buildContainer() {
    return new DependencyContainer();
  }

  private static UserManagementCli buildCli(final DependencyContainer container) {
    final Scanner scanner = new Scanner(System.in);
    return new UserManagementCli(container.userController(), new ConsoleIO(scanner, System.out));
  }
}
