package org.advisor.email.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.advisor.email.exceptions.AuthCodeIssueException;
import org.advisor.email.services.EmailAuthService;
import org.advisor.email.services.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailAuthService authService;
    private final EmailService emailService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "이메일 인증코드 발송")
    @ApiResponse(responseCode = "204", description = "인증 코드 발송 성공")
    @Parameters({
            @Parameter(name = "to", description = "인증 코드를 발송할 이메일 주소", required = true, in = ParameterIn.PATH) // in 속성 추가
    })
    @GetMapping("/auth/{to}")
    public void authCode(@PathVariable("to") String to) {
        if (!authService.sendCode(to)) {
            throw new AuthCodeIssueException();
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "이메일 인증 코드 검증")
    @ApiResponse(responseCode = "204", description = "인증 코드 검증 성공")
    @Parameters({
            @Parameter(name = "authCode", description = "검증할 인증 코드", required = false, in = ParameterIn.QUERY) // in 속성 추가
    })
    @GetMapping("/verify")
    public void verify(@RequestParam(name="authCode", required = false) Integer authCode) {
        authService.verify(authCode);
    }

    @Operation(summary = "이메일 발송")
    @ApiResponse(responseCode = "200", description = "이메일 발송 성공.")
    @Parameters({
            @Parameter(name = "tpl", description = "이메일 템플릿 이름", required = false, in = ParameterIn.PATH), // in 속성 추가
            @Parameter(name="file", description = "업로드 파일, 복수개 전송 가능", required = false) //in 속성 추가
    })
    @PostMapping({"", "/tpl/{tpl}"})
    public void sendEmail(@PathVariable(name="tpl", required = false) String tpl, @RequestPart(name="file", required = false) List<MultipartFile> files, @ModelAttribute RequestEmail form) {
        form.setFiles(files);
        tpl = StringUtils.hasText(tpl) ? tpl : "general";
        emailService.sendEmail(form, tpl);
    }


}