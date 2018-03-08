package org.icgc.dcc.song.server.jwt;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.expression.OAuth2ExpressionParser;
import org.springframework.security.oauth2.provider.expression.OAuth2SecurityExpressionMethods;

public class JWTMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    public JWTMethodSecurityExpressionHandler() {
        this.setExpressionParser(new OAuth2ExpressionParser(this.getExpressionParser()));
    }

    @Override
    public StandardEvaluationContext createEvaluationContextInternal(Authentication authentication, MethodInvocation mi) {
        StandardEvaluationContext ec = super.createEvaluationContextInternal(authentication, mi);
        ec.setVariable("oauth2", new OAuth2SecurityExpressionMethods(authentication));
        return ec;
    }
}
