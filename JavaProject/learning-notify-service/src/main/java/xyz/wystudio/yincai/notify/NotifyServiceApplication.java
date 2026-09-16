package xyz.wystudio.yincai.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"xyz.wystudio.yincai.notify", "xyz.wystudio.yincai.web", "xyz.wystudio.yincai.security", "xyz.wystudio.yincai.mybatis"})
public class NotifyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotifyServiceApplication.class, args);
    }
}
