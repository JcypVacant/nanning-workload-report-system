package com.cyp.nanningworkloadreportsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 文档配置类
 * 配置 API 文档的基本信息（标题、描述、版本、联系方式等）
 * 文档访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 文档基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("南宁通信段劳动用工工时工分统计管理系统 API 文档")
                        .description("""
                                系统功能模块包括：
                                - 登录认证与权限管理（Sa-Token RBAC）
                                - 组织架构管理（段-车间-工区三级树形结构）
                                - 人员信息管理与人员调动
                                - 用工项目字典管理（多级树形字典）
                                - 月度填报管理
                                - 工区工时工分填报
                                - 车间审核与退回
                                - 车间汇总与段级汇总
                                - 多维度统计分析
                                - Excel 导出（多级表头）
                                - 审核记录与操作日志追踪

                                权限分为三级：
                                - 段级管理员（SECTION_ADMIN）：全段数据访问
                                - 车间管理员（WORKSHOP_ADMIN）：本车间范围
                                - 工区填报员（AREA_REPORTER）：本工区范围
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("南宁通信段")
                                .email("admin@nanning-tx.com"))
                        .license(new License()
                                .name("内部使用")
                                .url("https://nanning-tx.com")));
    }
}
