package fa.training.backend_qlct.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Mở khóa cho tất cả API bắt đầu bằng /api
                .allowedOrigins("http://localhost:4200") // Chỉ cho phép Angular gọi
                .allowedMethods("GET", "POST", "PUT", "DELETE");
                // Bạn có thể thêm các cấu hình khác nếu cần, ví dụ: .allowedHeaders("*") để cho phép tất cả header
    }
}
