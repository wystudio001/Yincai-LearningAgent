package xyz.wystudio.yincai.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "xyz.wystudio.yincai.user",
    "xyz.wystudio.yincai.web",
    "xyz.wystudio.yincai.security",
    "xyz.wystudio.yincai.mybatis"
})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
