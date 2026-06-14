package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Collections;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:4200")
public class AiController {

    @Autowired
    private AiService aiService;

    @GetMapping("/advice")
    public ResponseEntity<?> getAdvice(@RequestParam(name = "username", required = false) String username) {
        String actualUsername = (username != null) ? username : "guest";
        String advice = aiService.getFinancialAdvice(actualUsername);

        // Trả về dạng JSON cho FE dễ đọc
        return ResponseEntity.ok(Collections.singletonMap("message", advice));
    }
}