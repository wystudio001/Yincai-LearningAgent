package xyz.wystudio.yincai.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"xyz.wystudio.yincai.auth", "xyz.wystudio.yincai.web", "xyz.wystudio.yincai.security", "xyz.wystudio.yincai.mybatis"})
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
