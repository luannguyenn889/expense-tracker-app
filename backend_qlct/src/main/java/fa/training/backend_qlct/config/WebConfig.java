package fa.training.backend_qlct.config; // Sửa lại package nếu cần

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho toàn bộ API
                .allowedOrigins("http://localhost:4200") // Phải chỉ định chính xác cổng của Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Bắt buộc phải cho phép OPTIONS
                .allowedHeaders("*")
                .allowCredentials(true); // Bắt buộc bật dòng này khi hệ thống có xài Token/Auth
    }
}