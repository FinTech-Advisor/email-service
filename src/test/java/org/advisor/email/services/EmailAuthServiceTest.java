package org.advisor.email.services;

import org.advisor.email.services.EmailAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"default", "test", "email"})
public class EmailAuthServiceTest {

    @Autowired
    private EmailAuthService service;

    @Test
    void test1() {
        boolean result = service.sendCode("kwat0112@gmail.com");
        System.out.println(result);
    }
}