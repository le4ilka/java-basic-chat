package ru.otus.chat.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBAuthenticationProvaider implements AuthenticatedProvaider, AutoCloseable {

    class User {
        private String login;
        private String password;
        private String username;
        private String userrole;

        public User(String login, String password, String username, String userrole) {
            this.login = login;
            this.password = password;
            this.username = username;
            this.userrole = userrole;
        }

        @Override
        public String toString() {
            return "User{" +
                    "login=" + login + '\'' +
                    "password=" + password + '\'' +
                    "role=" + userrole + '\'' +
                    "name=" + username + "}";

        }
    }

    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/chat";


    private Server server;
    private static Connection connection;
    List<User> users;

    public DBAuthenticationProvaider(Server server) {
        this.server = server;
        this.users = new ArrayList<>();
    }

    @Override
    public void initialize() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL, "postgres", "111111");
            Statement statement = connection.createStatement();
        } catch (Exception e) {
            System.out.println("Что-то поймали");
            e.printStackTrace();
        }

        System.out.println("Сервис аутентификации запущен: DB режим");
    }

    private static final String USER_NAME_BY_LOGIN_AND_PASS_QUERY = "SELECT username FROM USERS where login = ? and password = ?";

    private String getUsernameByLoginAndPassword(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(USER_NAME_BY_LOGIN_AND_PASS_QUERY)) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet resultSet = ps.executeQuery()) {
                resultSet.next();
                return resultSet.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String ROLE_BY_USERNAME_QUERY = "select rolename from roles join users on roles.id=users.role_id where username = ?";

    private String getUserroleByName(String name) {
        try (PreparedStatement ps = connection.prepareStatement(ROLE_BY_USERNAME_QUERY)) {
            ps.setString(1, name);
            try (ResultSet resultSet = ps.executeQuery()) {
                resultSet.next();
                return resultSet.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public boolean authenticate(ClientHandler clientHandler, String login, String password) {
        String authName = getUsernameByLoginAndPassword(login, password);
        if (authName == null) {
            clientHandler.sendMessage("Некорректный логин/пароль");
            return false;
        }
        if (server.isUsernameBusy(authName)) {
            clientHandler.sendMessage("Учетная запись уже занята");
            return false;
        }

        clientHandler.setUsername(authName);
        clientHandler.setUserrole(getUserroleByName(authName));
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/authok " + authName);
        return true;
    }

    private static final String LOGIN_QUERY = "select login from users";

    private boolean isLoginAlreadyExist(String login) {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(LOGIN_QUERY)) {
                while (resultSet.next()) {
                    if (resultSet.getString(1).equals(login)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final String USERNAME_QUERY = "select username from users";

    public boolean isUsernameAlreadyExist(String username) {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(USERNAME_QUERY)) {
                while (resultSet.next()) {
                    if (resultSet.getString(1).equals(username)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String username, String userrole) {
        if (login.trim().length() < 4 || password.trim().length() < 6
                || username.trim().length() < 2) {
            clientHandler.sendMessage("Требования логин 3+ символа, пароль 6+ символа," +
                    "имя пользователя 2+ символа не выполнены");
            return false;
        }
        if (isLoginAlreadyExist(login)) {
            clientHandler.sendMessage("Указанный логин уже занят");
            return false;
        }
        if (isUsernameAlreadyExist(username)) {
            clientHandler.sendMessage("Указанное имя пользователя уже занято");
            return false;
        }
        if (!(userrole.equals("ADMIN") || userrole.equals("USER"))) {
            clientHandler.sendMessage("Роль может быть только ADMIN или USER");
            return false;
        }
        users.add(new DBAuthenticationProvaider.User(login, password, username, userrole));
        clientHandler.setUsername(username);
        clientHandler.setUserrole(userrole);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/regok " + username);

        return true;

    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
