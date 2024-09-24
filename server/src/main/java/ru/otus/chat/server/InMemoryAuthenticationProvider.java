package ru.otus.chat.server;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthenticationProvider implements AuthenticatedProvaider{
    private class User {
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
    }

    private Server server;
    private List<User> users;

    public InMemoryAuthenticationProvider(Server server) {
        this.server = server;
        this.users = new ArrayList<>();
        this.users.add(new User("log1", "pass1", "bob1", "USER"));
        this.users.add(new User("log2", "pass2", "bob2", "ADMIN"));
        this.users.add(new User("log3", "pass3", "bob3", "USER"));
        this.users.add(new User("log3", "pass4", "bob4", "USER"));
    }

    @Override
    public void initialize() {
        System.out.println("Сервис аутентификации запущен: In memory режим");
    }

    private String getUsernameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.username;
            }
        }
        return null;
    }

    private String getUserroleByName(String name) {
        for (User user : users) {
            if (user.username.equals(name)) {
                return user.userrole;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean authenticate(ClientHandler clientHandler, String login, String password) {
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

    private boolean isLoginAlreadyExist(String login) {
        for (User user : users) {
            if (user.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsernameAlreadyExist(String username) {
        for (User user : users) {
            if (user.username.equals(username)) {
                return true;
            }
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
        users.add(new User(login, password, username, userrole));
        clientHandler.setUsername(username);
        clientHandler.setUserrole(userrole);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/regok " + username);

        return true;
    }
}
