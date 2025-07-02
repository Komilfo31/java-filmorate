package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.DataRetrievalException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Qualifier("dbUserStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u WHERE u.id = ?",
                    userRowMapper,
                    id
            );

            if (user != null) {
                getFriends(user.getId());
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new DataRetrievalException("Ошибка при получении пользователя из базы данных", e);
        }
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int updated = jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (updated == 0) {
            throw new NotFoundException("Пользователь не найден");
        }

        return getUserById(user.getId()).orElse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);

        if (!users.isEmpty()) {
            Map<Long, List<User>> friendsByUser = getFriendsForUsers(
                    users.stream().map(User::getId).collect(Collectors.toList())
            );

            users.forEach(user -> {
                Set<Long> friendIds = friendsByUser.getOrDefault(user.getId(), Collections.emptyList())
                        .stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());
                user.getFriends();
            });
        }

        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)",
                Boolean.class,
                id
        );
        return exists != null && exists;
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        return user.getId() == null ? createUser(user) : updateUser(user);
    }

    @Override
    @Transactional
    public void addFriend(long userId, long friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    @Transactional
    public void removeFriend(long userId, long friendId) {
        // Проверяем существование пользователей
        if (!exists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!exists(friendId)) {
            throw new NotFoundException("Друг не найден");
        }

        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );

        getUserById(userId).ifPresent(user -> user.getFriends().remove(friendId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getFriends(long userId) {
        // Проверяем существование пользователя
        if (!exists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getCommonFriends(long userId, long otherId) {
        String sql = """
                SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u
                JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = ?
                JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = ?
                """;
        return jdbcTemplate.query(sql, userRowMapper, userId, otherId);
    }

    public Map<Long, List<User>> getFriendsForUsers(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = userIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(
                "SELECT f.user_id, u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM friendships f " +
                        "JOIN users u ON f.friend_id = u.id " +
                        "WHERE f.user_id IN (%s)",
                placeholders
        );

        Object[] params = userIds.toArray();

        Map<Long, List<User>> result = new HashMap<>();

        jdbcTemplate.query(sql, params, rs -> {
            Long userId = rs.getLong("user_id");
            User friend = new User(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate()
            );
            result.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(friend);
        });

        return result;
    }
}