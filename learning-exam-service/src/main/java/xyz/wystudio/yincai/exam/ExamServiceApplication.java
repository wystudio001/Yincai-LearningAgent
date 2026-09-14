package xyz.wystudio.yincai.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"xyz.wystudio.yincai.exam", "xyz.wystudio.yincai.web", "xyz.wystudio.yincai.security", "xyz.wystudio.yincai.mybatis"})
public class ExamServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExamServiceApplication.class, args);
    }
}
