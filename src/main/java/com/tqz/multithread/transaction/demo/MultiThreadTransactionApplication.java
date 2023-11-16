package com.tqz.multithread.transaction.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author tianqingzhao
 * @since 2023/11/16 9:48
 */
@MapperScan("com.tqz.multithread.transaction.demo.mapper")
@SpringBootApplication
public class MultiThreadTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiThreadTransactionApplication.class, args);
    }
}
