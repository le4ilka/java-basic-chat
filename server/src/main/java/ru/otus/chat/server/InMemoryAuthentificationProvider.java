package ru.otus.chat.server;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthentificationProvider implements AuthentificatedProvaider {
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

        public String getUserrole(){
            return userrole;
        }

        public String getUsername(){
            return username;
        }
    }

    private Server server;
    private List<User> users;

    public InMemoryAuthentificationProvider(Server server) {
        this.server = server;
        this.users = new ArrayList<>();
        this.users.add(new User("login1", "password1", "bob1", "USER"));
        this.users.add(new User("login2", "password2", "bob2", "ADMIN"));
        this.users.add(new User("login3", "password3", "bob3", "USER"));
        this.users.add(new User("login4", "password4", "bob4", "USER"));
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

    public String getUserroleByUsername(String username){
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(username)){
                return users.get(i).getUserrole();
            }
        }
        return "такой пользователь не зарегистрирован";
    }

    @Override
    public synchronized boolean authentificate(ClientHandler clientHandler, String login, String password) {
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
        if (login.trim().length() < 3 || password.trim().length() < 6
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
        users.add(new User(login, password, username, userrole));
        clientHandler.setUsername(username);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/regok " + username);

        return true;
    }


}
