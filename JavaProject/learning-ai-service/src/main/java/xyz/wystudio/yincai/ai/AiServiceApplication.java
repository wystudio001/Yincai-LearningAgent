package xyz.wystudio.yincai.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "xyz.wystudio.yincai.ai", "xyz.wystudio.yincai.web",
        "xyz.wystudio.yincai.security", "xyz.wystudio.yincai.mybatis" })
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
