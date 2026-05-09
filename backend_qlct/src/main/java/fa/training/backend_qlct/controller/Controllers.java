package fa.training.backend_qlct.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controllers {
    @GetMapping("/hello")
    public String getString(){
        return "Hello World";
    }

}
