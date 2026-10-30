package com.na.admin;

import com.na.common.annotation.EnableNA;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.net.InetAddress;

/**
 * @author na
 * @date 2024年11月05日
 */

@Slf4j
@SpringBootApplication
@EnableScheduling //开启定时任务
@EnableAsync
@EnableNA
@ComponentScan(basePackages = {"com.na"}) // 扫描多个包路径
@MapperScan(basePackages = {"com.na.core.mapper"}) // 扫描指定的 Mapper 包
public class AdminApplication implements CommandLineRunner {
    @Autowired
    private Environment env;


    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(AdminApplication.class, args);
        } catch (Exception e) {
            System.err.println("❌ admin 启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // 非 Windows 系统时创建上传目录
            String multipartLocation = env.getProperty("spring.servlet.multipart.location", "/na/tmp");
            File dir = new File(multipartLocation);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            log.info("✅ 上传目录已初始化: {}", multipartLocation);

            InetAddress address = InetAddress.getLocalHost();
            String host = address.getHostAddress();
            String port = env.getProperty("server.port", "");

            log.info("🚀 admin 启动成功");
            log.info("📘 接口文档地址: http://{}:{}/doc.html", host, port);
            log.info("📘 druid文档地址: http://{}:{}/druid", host, port);

        } catch (Exception e) {
            log.error("启动后初始化异常: {}", e.getMessage(), e);
        }
    }
}
