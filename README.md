# Схема базы данных

## Диаграмма
[![Схема БД](https://app.quickdatabasediagrams.com/#/d/DStPiX)](https://app.quickdatabasediagrams.com/#/d/DStPiX)

## Описание таблиц

### Основные таблицы
- **films** - Основная информация о фильмах:
  - `id` - уникальный идентификатор
  - `name` - название фильма
  - `description` - описание
  - `release_date` - дата выхода
  - `duration` - продолжительность в минутах
  - `mpa_rating_id` - возрастной рейтинг (связь с mpa_ratings)

- **mpa_ratings** - Возрастные рейтинги:
  - `id` - код рейтинга
  - `name` - название (G, PG, PG-13, R, NC-17)

- **users** - Пользователи системы:
  - `id` - уникальный идентификатор
  - `email` - электронная почта
  - `login` - логин
  - `name` - имя (может быть пустым)
  - `birthday` - дата рождения

- **genres** - Жанры фильмов:
  - `id` - уникальный идентификатор
  - `name` - название жанра

### Связующие таблицы
- **film_genres** - Связь фильмов и жанров:
  - `film_id` - ID фильма
  - `genre_id` - ID жанра

- **film_likes** - Лайки пользователей:
  - `film_id` - ID фильма
  - `user_id` - ID пользователя

- **friendships** - Дружеские связи:
  - `user_id` - ID пользователя
  - `friend_id` - ID друга

## Примеры SQL-запросов

### 1. Топ-5 популярных фильмов
```sql
SELECT f.id, f.name, COUNT(fl.user_id) AS likes_count
FROM films f
LEFT JOIN film_likes fl ON f.id = fl.film_id
GROUP BY f.id
ORDER BY likes_count DESC
LIMIT 5;
```
### 2. Общие друзья двух пользователей
```sql
SELECT u.id, u.login, u.name
FROM users u
JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = 123
JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = 456;
```
### 3. Фильмы по жанру
```sql
SELECT f.* 
FROM films f
JOIN film_genres fg ON f.id = fg.film_id
WHERE fg.genre_id = 1; -- 1 = Комедия
```
### 4. Добавление друга
```sql
INSERT INTO friendships (user_id, friend_id)
VALUES (123, 456);
```
### 5. Найти пользователей, у которых больше всего друзей
```sql
SELECT u.id, u.login, COUNT(f.friend_id) AS friends_count
FROM users u
LEFT JOIN friendships f ON u.id = f.user_id
GROUP BY u.id
ORDER BY friends_count DESC
LIMIT 10;
```
### 6. Найти фильмы, которые понравились друзьям пользователя
```sql
SELECT DISTINCT f.id, f.name, COUNT(fl.user_id) AS friend_likes
FROM films f
JOIN film_likes fl ON f.id = fl.film_id
JOIN friendships fr ON fl.user_id = fr.friend_id
WHERE fr.user_id = 123
GROUP BY f.id
ORDER BY friend_likes DESC;
```
### 7. Получить статистику по жанрам
```sql
SELECT 
    g.name AS genre,
    COUNT(fg.film_id) AS films_count,
    AVG(f.duration) AS avg_duration
FROM genres g
LEFT JOIN film_genres fg ON g.id = fg.genre_id
LEFT JOIN films f ON fg.film_id = f.id
GROUP BY g.id
ORDER BY films_count DESC;
```
### 8. Самые популярные жанры
```sql
SELECT 
    g.name AS genre,
    COUNT(DISTINCT fg.film_id) AS total_films,
    COUNT(fl.user_id) AS total_likes
FROM genres g
LEFT JOIN film_genres fg ON g.id = fg.genre_id
LEFT JOIN film_likes fl ON fg.film_id = fl.film_id
GROUP BY g.id
ORDER BY total_likes DESC;
```
### 9. Фильмы без лайков
```sql
SELECT f.id, f.name
FROM films f
LEFT JOIN film_likes fl ON f.id = fl.film_id
WHERE fl.user_id IS NULL;
```