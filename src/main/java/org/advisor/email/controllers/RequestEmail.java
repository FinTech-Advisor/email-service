package org.advisor.email.controllers;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
public class RequestEmail {
    private List<String> to; // 받는쪽 이메일
    private List<String> cc; // 참조
    private List<String> bcc; // 숨은참조
    private String subject; // 메일 제목
    private String content; // 메일 내용
    private Map<String, Object> data;
    private List<MultipartFile> files;
}