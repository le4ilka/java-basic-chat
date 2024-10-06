package ru.otus.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthenticatedProvaider authenticatedProvider;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
        authenticatedProvider = new InMemoryAuthenticationProvider(this);
        authenticatedProvider.initialize();
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public AuthenticatedProvaider getAuthenticatedProvider() {
        return authenticatedProvider;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMessage(clientHandler.getUsername() + " connected");
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage(clientHandler.getUsername() + " leaves chat");
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void privateMessage(String name, String message) {
        for (ClientHandler client : clients) {
            if (name.equals(client.getUsername())) {
                client.sendMessage(message);
            }
        }
    }
}
