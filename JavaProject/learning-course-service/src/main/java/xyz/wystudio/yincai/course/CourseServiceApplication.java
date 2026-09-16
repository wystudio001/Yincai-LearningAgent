package xyz.wystudio.yincai.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"xyz.wystudio.yincai.course", "xyz.wystudio.yincai.web", "xyz.wystudio.yincai.security", "xyz.wystudio.yincai.mybatis"})
public class CourseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApplication.class, args);
    }
}
