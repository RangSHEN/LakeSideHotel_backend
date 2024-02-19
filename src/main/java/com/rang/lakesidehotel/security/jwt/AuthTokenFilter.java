package com.rang.lakesidehotel.security.jwt;

import com.rang.lakesidehotel.security.user.HotelUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//filter this token to validate all the method have declared here to make sure that this token is authenticated
//@Component 注解可能会导致它在整个应用程序上下文中的每个请求都被注册为一个 bean 过滤器都用@Bean
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private HotelUserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //每一次请求都要解析jwt 然后重新校验
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateToken(jwt)){
                String email = jwtUtils.getUserNameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication); //token is authenticated
            }
        }catch (Exception e){
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request,response);

    }

    private String parseJwt(HttpServletRequest request) {
        String headAuth = request.getHeader("Authorization");
        if(StringUtils.hasText(headAuth) && headAuth.startsWith("Bearer ")){
            return headAuth.substring(7);
        }
        return null;

    }
}
//doFilterInternal 就像是一个特定的过滤器的实际工作内容，处理每个请求的具体逻辑。
//在这里你写的是怎么处理用户请求，比如验证用户的身份、权限等。
//这个方法是真正处理请求的地方。

///doFilterInternal 方法：
//        就像是一位具体的工作者，专门负责处理每个人的请求。
//        想象一下，你有一位专门的服务员，当有人点餐时，服务员会负责确认顾客的身份，检查他们的权限，然后为他们提供所需的服务。这个服务员就是 doFilterInternal。
//
//filterChain 就像是一位整体规划者，决定了整个餐厅的经营策略。
//        想象一下，你是一家餐厅的老板，你制定了一些规定，比如所有服务员都必须穿制服，禁止顾客带外卖进来等。filterChain 就像是这位老板，决定了整个餐厅的运营规则。