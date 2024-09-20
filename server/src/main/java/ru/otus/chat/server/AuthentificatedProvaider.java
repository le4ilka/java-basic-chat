package ru.otus.chat.server;

public interface AuthentificatedProvaider {
    void initialize();

    boolean authentificate(ClientHandler clientHandler, String login, String password);

    boolean registration(ClientHandler clientHandler, String login, String password, String username, String userrole);

    String getUserroleByUsername(String username);
}


