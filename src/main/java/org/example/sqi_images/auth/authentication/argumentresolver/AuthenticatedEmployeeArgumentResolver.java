package org.example.sqi_images.auth.authentication.argumentresolver;

import lombok.RequiredArgsConstructor;
import org.example.sqi_images.auth.authentication.AuthenticationContext;
import org.example.sqi_images.auth.authentication.annotation.AuthEmployee;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Configuration
@RequiredArgsConstructor
public class AuthenticatedEmployeeArgumentResolver implements HandlerMethodArgumentResolver {
    private final AuthenticationContext authenticationContext;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthEmployee.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter,
                                  final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest,
                                  final WebDataBinderFactory binderFactory) throws Exception {
        return authenticationContext.getPrincipal();
    }
}
