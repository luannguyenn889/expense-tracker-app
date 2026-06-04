// package fa.training.backend_qlct.service;

// import java.util.List;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.http.client.SimpleClientHttpRequestFactory;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// @Service
// public class AiService {
//     @Value("${gemini.api.key}")
//     private String apiKey;

//     @Value("${gemini.api.url}")
//     private String apiUrl;

//     // Các Repository cần thiết để lấy dữ liệu
//     // @Autowired private TransactionRepository transactionRepository;

//     public String getFinancialAdvice(String username) {
//         // 1. TỔNG HỢP DỮ LIỆU TỪ DATABASE
//         // Giả sử bạn viết query lấy ra tổng thu, tổng chi và các danh mục chi nhiều nhất tháng này
//         // double totalIncome = ...;
//         // double totalExpense = ...;
//         // String topCategories = "Ăn uống: 3.000.000đ, Mua sắm: 2.000.000đ";

//         // Dữ liệu giả lập để test:
//         double totalIncome = 15000000;
//         double totalExpense = 12000000;
//         String topCategories = "Ăn uống (5tr), Giải trí (4tr)";

//         // 2. KỸ THUẬT PROMPT ENGINEERING
//         String prompt = String.format(
//                 "Đóng vai một chuyên gia tài chính. Tháng này tôi có thu nhập %,.0f VNĐ, " +
//                         "đã chi tiêu %,.0f VNĐ. Các khoản chi lớn nhất gồm: %s. " +
//                         "Hãy đưa ra 3 lời khuyên ngắn gọn (dưới 100 chữ) để tôi quản lý tiền tốt hơn.",
//                 totalIncome, totalExpense, topCategories
//         );

//         // 3. GỌI API GEMINI (Đóng gói JSON theo chuẩn tài liệu của Google)
//         SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//         factory.setConnectTimeout(5000); // 5 seconds connect timeout
//         factory.setReadTimeout(15000);    // 15 seconds read timeout
//         RestTemplate restTemplate = new RestTemplate(factory);

//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);

//         String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + prompt + "\" }] }] }";
//         HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

//         try {
//             ResponseEntity<Map> response = restTemplate.postForEntity(
//                     apiUrl + "?key=" + apiKey, request, Map.class
//             );

//             // 4. BÓC TÁCH KẾT QUẢ TRẢ VỀ
//             // Đoạn này phụ thuộc vào cấu trúc JSON của Gemini, thường nó nằm sâu bên trong mảng candidates
//             Map<String, Object> body = response.getBody();
//             List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
//             Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
//             List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

//             return (String) parts.get(0).get("text"); // Trả về câu trả lời của AI

//         } catch (Exception e) {
//             e.printStackTrace();
//             System.err.println("Lỗi gọi AI: " + e.getMessage());
//             return "Hiện tại hệ thống AI đang quá tải, vui lòng quay lại sau nhé!";
//         }
//     }
// }
