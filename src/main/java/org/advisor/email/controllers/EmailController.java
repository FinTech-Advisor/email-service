package org.advisor.email.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.advisor.email.exceptions.AuthCodeIssueException;
import org.advisor.email.services.EmailAuthService;
import org.advisor.email.services.EmailResetService;
import org.advisor.email.services.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailAuthService authService;
    private final EmailService emailService;
    private final EmailResetService ResetService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "이메일 인증코드 발송.")
    @ApiResponse(responseCode = "201", description = "인증 코드 발송 성공")
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
    @Operation(summary = "이메일 인증 코드 검증.")
    @ApiResponse(responseCode = "200", description = "인증 코드 검증 성공")
    @Parameters({
            @Parameter(name = "authCode", description = "검증할 인증 코드", required = false, in = ParameterIn.QUERY) // in 속성 추가
    })
    @GetMapping("/verify")
    public void verify(@RequestParam(name="authCode", required = false) Integer authCode) {
        authService.verify(authCode);
    }

    @Operation(summary = "이메일 발송")
    @ApiResponse(responseCode = "200", description = "이메일 발송 성공") // 201 -> 200
    @Parameters({
            @Parameter(name = "tpl", description = "이메일 템플릿 이름", required = false, in = ParameterIn.PATH),
            @Parameter(name = "file", description = "업로드 파일, 복수개 전송 가능", required = false, in = ParameterIn.PATH)
    })
    @PostMapping({"/" , "/tpl/{tpl}"})
    public ResponseEntity<EmailResponse> sendEmail(@PathVariable(name="tpl", required = false) String tpl, @RequestPart(name="file", required = false) List<MultipartFile> files, @RequestBody RequestEmail form){
        try {
            // tpl 값이 null일 경우, 기본 템플릿 이름(예: "general")을 사용
            String templateName = (tpl == null) ? "general" : tpl;
            boolean success = emailService.sendEmail(form, templateName);
            return ResponseEntity.ok(new EmailResponse(success ? "이메일 발송 성공" : "이메일 발송 실패"));
        } catch(Exception e) {
            // 기타 예외 처리 (EmailService에서 처리되지 않은 예외)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new EmailResponse("이메일 발송 중 오류가 발생했습니다."));
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "비밀번호 초기화 확인") // 요약 수정
    @ApiResponse(responseCode = "201", description = "비밀번호 초기화 확인 성공")
    @Parameters({
            @Parameter(name = "token", description = "비밀번호 초기화 토큰", required = true, in = ParameterIn.QUERY) // 파라미터 설명 수정
    })
    @GetMapping("/reset-password/verify") // URL 및 메서드 이름 수정
    public void verifyPasswordReset(@RequestParam("token") String token) {
        ResetService.confirmResetEmail(token);
    }


}