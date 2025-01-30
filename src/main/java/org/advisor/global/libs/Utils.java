package org.advisor.global.libs;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.advisor.global.entities.CodeValue;
import org.advisor.global.repositories.CodeValueRepository;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Lazy
@Component
@RequiredArgsConstructor
public class Utils {

    private final MessageSource messageSource;
    private final DiscoveryClient discoveryClient;
    private final CodeValueRepository codeValueRepository;

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    /**
     * 메서지 코드로 조회된 문구
     *
     * @param code
     * @return
     */
    public String getMessage(String code) {
        HttpServletRequest request = getRequest();
        Locale lo = request.getLocale();
        try {
            return messageSource.getMessage(code, null, lo);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public List<String> getMessages(String[] codes) {

        return Arrays.stream(codes).map(c -> {
            try {
                return getMessage(c);
            } catch (Exception e) {
                return "";
            }
        }).filter(s -> !s.isBlank()).toList();

    }

    /**
     * REST 커맨드 객체 검증 실패시에 에러 코드를 가지고 메세지 추출
     *
     * @param errors
     * @return
     */
    public Map<String, List<String>> getErrorMessages(Errors errors) {
        // 필드별 에러코드 - getFieldErrors()
        // Collectors.toMap
        Map<String, List<String>> messages = errors.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, f -> getMessages(f.getCodes()), (v1, v2) -> v2));

        // 글로벌 에러코드 - getGlobalErrors()
        List<String> gMessages = errors.getGlobalErrors()
                .stream()
                .flatMap(o -> getMessages(o.getCodes()).stream())
                .toList();
        // 글로벌 에러코드 필드 - global
        if (!gMessages.isEmpty()) {
            messages.put("global", gMessages);
        }

        return messages;
    }

    /**
     * 유레카 서버 인스턴스 주소 검색
     *
     * spring.profiles.active : dev - localhost로 되어 있는 주소를 반환
     * - 예) member-service : 최대 2가지만 존재, 1 - 실 서비스 도메인 주소, 2. localhost ...
     *
     * @param serviceId
     * @param url
     * @return
     */
    public String serviceUrl(String serviceId, String url) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            String profile = System.getenv("spring.profiles.active");
            boolean isDev = StringUtils.hasText(profile) && profile.contains("dev");
            String serviceUrl = null;
            for (ServiceInstance instance : instances) {
                String uri = instance.getUri().toString();
                if (isDev && uri.contains("localhost")) {
                    serviceUrl = uri;
                } else if (!isDev && !uri.contains("localhost")) {
                    serviceUrl = uri;
                }
            }

            if (StringUtils.hasText(serviceUrl)) {
                return serviceUrl + url;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }

    /**
     * 요청 헤더 : Authorizaion: Bearer ...
     *
     * @return
     */
    public String getAuthToken() {
        HttpServletRequest request = getRequest();
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            return "";
        }
        return auth.substring(7).trim();
    }

    /**
     * 전체 주소
     *
     * @param url
     * @return
     */
    public String getUrl(String url) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "";
        }
        return String.format("%s://%s:%d%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath(), url);
    }

    /**
     * 사용자 구분을 위한 해시값 조회
     *
     * @return
     */
    public String getUserHash() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String userKey = "userHash";

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(userKey)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * Code - Value 값 저장
     *
     * @param code
     * @param value
     */
    public <T> void saveValue(String code, T value) {
        CodeValue codeValue = new CodeValue();
        codeValue.setCode(code);
        codeValue.setValue(value);
        codeValueRepository.save(codeValue);
    }

    /**
     * code로 값 조회
     *
     * @param code
     * @return
     */
    public <T> T getValue(String code) {
        CodeValue data = codeValueRepository.findByCode(code);

        return data == null ? null : (T) data.getValue();
    }
}