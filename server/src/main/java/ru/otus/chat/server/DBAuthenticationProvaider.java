//package ru.otus.chat.server;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.Statement;
//
//public class DBAuthenticationProvaider implements AuthenticatedProvaider{
//    private server;
//    private static Connection connection;
//    private static Statement statement;
//
//    public DBAuthenticationProvider(Server server) {
//        this.server = server;
//    }
//
//    @Override
//    public void initialize() {
//        try {
//            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/chat");
//            statement = connection.createStatement();
//        }catch (Exception e){
//            System.out.println("Что-то поймали");
//            e.printStackTrace();
//        }
//
//        System.out.println("Сервис аутентификации запущен: DB режим");
//    }
//
//    @Override
//    public boolean authenticate(ClientHandler clientHandler, String login, String password) {
//        return false;
//    }
//
//    @Override
//    public boolean registration(ClientHandler clientHandler, String login, String password, String username, String userrole) {
//        return false;
//    }
//}
