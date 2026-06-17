package com.smartvocab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智绘记 SmartVocab 启动类
 */
@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@MapperScan("com.smartvocab.module.**.mapper")
public class SmartVocabApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartVocabApplication.class, args);
        System.out.println("\n" +
                "  ____                  _   __     __              _    \n" +
                " / ___| _ __ ___   __ _(_)_\\ \\   / /_ _ _ __   __| | ___  _ __ ___ \n" +
                " \\___ \\| '_ ` _ \\ / _` | | __\\ \\ / / _` | '_ \\ / _` |/ _ \\| '_ ` _ \\\n" +
                "  ___) | | | | | | (_| | | |_ \\ V / (_| | | | | (_| | (_) | | | | | |\n" +
                " |____/|_| |_| |_|\\__,_|_|\\__| \\_/ \\__,_|_| |_|\\__,_|\\___/|_| |_| |_|\n" +
                "                                                                  \n" +
                "  智绘记 SmartVocab 启动成功  ->  http://localhost:8080/api\n");
    }
}
