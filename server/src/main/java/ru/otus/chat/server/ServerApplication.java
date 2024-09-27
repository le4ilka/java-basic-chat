package ru.otus.chat.server;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServerApplication {

    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/chat";
    private static final String USERS_QUERY = "SELECT * FROM USERS";

    public static void main(String[] args) throws SQLException {
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
            public String toString(){
                return "User{" +
                        "login=" + login + '\'' +
                        "password=" + password + '\'' +
                        "role=" + userrole + '\'' +
                        "name=" + username + "}";

            }
        }
        List<User> users = new ArrayList<>();
        //List<InMemoryAuthenticationProvider.User> = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, "postgres", "111111");) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(USERS_QUERY)) {
                    System.out.println("QUERY START");
                    while (resultSet.next()){
                        int id = resultSet.getInt("id");
                        String login = resultSet.getString(2);
                        String password = resultSet.getString(3);
                        String role = resultSet.getString(4);
                        String username = resultSet.getString(5);
                        User user = new User(login, password, username, role);
                        users.add(user);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(users);


        new Server(8189).start();
    }
}