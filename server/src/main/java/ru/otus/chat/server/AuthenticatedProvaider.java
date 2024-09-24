package ru.otus.chat.server;

public interface AuthenticatedProvaider {
    void initialize();

    boolean authenticate(ClientHandler clientHandler, String login, String password);

    boolean registration(ClientHandler clientHandler, String login, String password, String username, String userrole);
}


