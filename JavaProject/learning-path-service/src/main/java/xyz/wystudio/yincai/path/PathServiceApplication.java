package xyz.wystudio.yincai.path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"xyz.wystudio.yincai.path", "xyz.wystudio.yincai.web", "xyz.wystudio.yincai.security", "xyz.wystudio.yincai.mybatis"})
public class PathServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PathServiceApplication.class, args);
    }
}
