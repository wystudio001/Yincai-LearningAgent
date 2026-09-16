package xyz.wystudio.yincai.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"xyz.wystudio.yincai.profile", "xyz.wystudio.yincai.web", "xyz.wystudio.yincai.security", "xyz.wystudio.yincai.mybatis"})
public class ProfileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }
}
