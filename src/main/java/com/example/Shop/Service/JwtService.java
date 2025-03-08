//package com.example.Shop.Service;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//import com.example.Shop.model.User;
//
//import java.security.Key;
//import java.util.Date;
//
//@Service // Позначає цей клас як сервісний компонент Spring
//public class JwtService {
//    private final String SECRET_KEY = "FaYlrPMjDMAor1azOPb0J74YitRmG7mfBey8cbXDe5E8AdXqxVjRPTGeWsW3qpbD"; // Секретний ключ для підпису токенів
//
//    // Метод для генерації JWT-токена
//    public String generateJwtToken(User user) {
//        return Jwts.builder()
//                .setSubject(user.getUsername()) // Встановлює ім'я користувача як Subject токена
//                .setIssuedAt(new Date()) // Встановлює дату створення токена
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // Встановлює термін дії токена (1 година)
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Підписує токен за допомогою алгоритму HS256 і секретного ключа
//                .compact(); // Генерує токен у вигляді рядка
//    }
//
//    // Метод для отримання імені користувача (username) з JWT-токена
//    public String extractUsername(String token) {
//        return Jwts.parser() // Ініціалізує парсер JWT
//                .setSigningKey(getSigningKey()) // Встановлює ключ підпису для перевірки токена
//                .build()
//                .parseClaimsJws(token) // Розбирає токен і перевіряє підпис
//                .getBody()
//                .getSubject(); // Отримує значення Subject (username)
//    }
//
//    // Метод для перевірки дійсності токена
//    public boolean validateToken(String token, UserDetails userDetails) {
//        final String username = extractUsername(token); // Отримує ім'я користувача з токена
//        return username.equals(userDetails.getUsername()) && !isTokenExpired(token); // Повертає true, якщо токен дійсний і не прострочений
//    }
//
//    // Метод перевіряє, чи токен протермінований
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date()); // Порівнює дату закінчення дії токена з поточною датою
//    }
//
//    // Метод отримує дату закінчення дії токена
//    private Date extractExpiration(String token) {
//        return extractClaims(token).getExpiration();
//    }
//
//    // Метод отримує всі claims (дані) з токена
//    private Claims extractClaims(String token) {
//        return Jwts.parser()
//                .setSigningKey(getSigningKey()) // Встановлює ключ для перевірки підпису
//                .build()
//                .parseClaimsJws(token) // Розбирає токен і перевіряє його підпис
//                .getBody(); // Повертає claims (інформацію, що міститься в токені)
//    }
//
//    // Метод для отримання ключа підпису
//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes()); // Генерує ключ для підпису токена
//    }
//}
