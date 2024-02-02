package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties properties) {
        try {
            Class.forName(properties.getProperty("jdbc.driver"));
            String connectionURL = properties.getProperty("url");
            String username = properties.getProperty("login");
            String password = properties.getProperty("password");
            connection = DriverManager.getConnection(connectionURL, username, password);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Properties getProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream reader = PsqlStore.class
                .getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(reader);
        }
        return properties;
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement preparedStatement = connection
                .prepareStatement("INSERT INTO post (name, text, link, created)"
                        + "VALUES (?, ?, ?, ?)"
                        + "ON CONFLICT (link)"
                        + "DO NOTHING")) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setTimestamp(4, Timestamp
                    .valueOf(new Timestamp(System.currentTimeMillis())
                            .toLocalDateTime().withNano(0)));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement preparedStatement
                = connection.prepareStatement("SELECT * FROM post")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    postList.add(createNewPost(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return postList;
    }

    private Post createNewPost(ResultSet resultSet) throws SQLException {
            return new Post(resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("text"),
                    resultSet.getString("link"),
                    resultSet.getTimestamp("created").toLocalDateTime());
        }

    @Override
    public Post findById(int id) {
        Post postById = null;
        try (PreparedStatement preparedStatement
                = connection.prepareStatement("SELECT * FROM post")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (Objects.equals(resultSet.getInt("id"), id)) {
                        postById = createNewPost(resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return postById;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) throws Exception {
        try (PsqlStore psqlStore = new PsqlStore(getProperties())) {
            psqlStore.save(new Post(1, "job", "http:url", "description", LocalDateTime.parse(LocalDateTime.now().withNano(0).format(ISO_LOCAL_DATE_TIME))));
            psqlStore.save(new Post(2, "job2", "http:url2", "description2", LocalDateTime.parse(LocalDateTime.now().withNano(0).format(ISO_LOCAL_DATE_TIME))));
            System.out.println(psqlStore.findById(1));
            System.out.println(psqlStore.getAll());
        }
    }
}