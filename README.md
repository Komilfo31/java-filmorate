# Схема базы данных

## Диаграмма
[![Схема БД](https://app.quickdatabasediagrams.com/#/d/DStPiX)](https://app.quickdatabasediagrams.com/#/d/DStPiX)

## Описание таблиц

### Основные таблицы
- **films** - Основная информация о фильмах:
    - Название, описание
    - Дата выхода
    - Продолжительность (в минутах)
    - Возрастной рейтинг (связь с MPA)

- **mpa_ratings** - Возрастные рейтинги:
    - Коды (G, PG, PG-13, R, NC-17)
    - Полное описание ограничений

- **users** - Пользователи системы:
    - Email, логин
    - Имя
    - Дата рождения

### Связующие таблицы
- **film_genres** - Связь фильмов и жанров:
    - Один фильм → несколько жанров
    - Жанры хранятся как ENUM (COMEDY, DRAMA и т.д.)

- **film_likes** - Лайки фильмов:
    - Кто какой фильм лайкнул
    - Связь пользователь → фильм

- **friendships** - Дружеские связи:
    - Отправленные/принятые заявки
    - Статусы (PENDING/CONFIRMED)

## Примеры SQL-запросов

### 1. Топ-5 популярных фильмов
```sql
SELECT f.film_id, f.name, COUNT(fl.user_id) AS likes_count
FROM films f
LEFT JOIN film_likes fl ON f.film_id = fl.film_id
GROUP BY f.film_id, f.name
ORDER BY likes_count DESC
LIMIT 5;
```
### 2. Общие друзья двух пользователей
```sql
SELECT u.user_id, u.login, u.name
FROM users u
JOIN friendships f1 ON u.user_id = f1.friend_id AND f1.user_id = 123
JOIN friendships f2 ON u.user_id = f2.friend_id AND f2.user_id = 456
WHERE f1.status = 'ПОДТВЕРЖДЕНО' AND f2.status = 'ПОДТВЕРЖДЕНО';
```
### 3. Фильмы по жанру
```sql
SELECT f.* 
FROM films f
JOIN film_genres fg ON f.film_id = fg.film_id
WHERE fg.genre = 'КОМЕДИЯ';
```
### 4. Обновление статуса дружбы
```sql
UPDATE friendships
SET status = 'ПОДТВЕРЖДЕНО'
WHERE user_id = 123 AND friend_id = 456;
```
### 5. Найти пользователей, у которых больше всего друзей
```sql
SELECT u.user_id, u.login, COUNT(f.friend_id) AS friends_count
FROM users u
LEFT JOIN friendships f ON u.user_id = f.user_id AND f.status = 'CONFIRMED'
GROUP BY u.user_id
ORDER BY friends_count DESC
LIMIT 10;
```
### 6. Найти фильмы, которые понравились друзьям пользователя
```sql
SELECT DISTINCT f.film_id, f.name, COUNT(fl.user_id) AS friend_likes
FROM films f
JOIN film_likes fl ON f.film_id = fl.film_id
JOIN friendships fr ON fl.user_id = fr.friend_id AND fr.status = 'CONFIRMED'
WHERE fr.user_id = 123
GROUP BY f.film_id
ORDER BY friend_likes DESC;
```
### 7. Получить статистику по жанрам
```sql
SELECT 
    fg.genre,
    COUNT(*) AS films_count,
    AVG(f.duration) AS avg_duration
FROM film_genres fg
JOIN films f ON fg.film_id = f.film_id
GROUP BY fg.genre
ORDER BY films_count DESC;
```
### 8. Самые популярные жанры
```sql
SELECT 
    fg.genre,
    COUNT(DISTINCT fg.film_id) AS total_films,
    COUNT(fl.user_id) AS total_likes
FROM film_genres fg
LEFT JOIN film_likes fl ON fg.film_id = fl.film_id
GROUP BY fg.genre
ORDER BY total_likes DESC;
```
### 9. Фильмы без лайков
```sql
SELECT f.film_id, f.name
FROM films f
LEFT JOIN film_likes fl ON f.film_id = fl.film_id
WHERE fl.user_id IS NULL;
```