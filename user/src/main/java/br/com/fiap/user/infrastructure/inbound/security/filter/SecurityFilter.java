package br.com.fiap.user.infrastructure.inbound.security.filter;

import br.com.fiap.user.application.ports.inbound.auth.ForGettingUserByToken;
import br.com.fiap.user.application.ports.inbound.auth.ForValidateToken;
import br.com.fiap.user.application.ports.inbound.auth.output.GetUserByTokenOutput;
import br.com.fiap.user.infrastructure.inbound.security.model.AddressDetails;
import br.com.fiap.user.infrastructure.inbound.security.model.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final ForGettingUserByToken forGettingUserByToken;
    private final ForValidateToken forValidateToken;

    public SecurityFilter(ForGettingUserByToken forGettingUserByToken, ForValidateToken forValidateToken) {
        this.forGettingUserByToken = forGettingUserByToken;
        this.forValidateToken = forValidateToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = this.recoverToken(request);

        if (token != null && !token.isBlank()) {
            forValidateToken.validateToken(token);

            GetUserByTokenOutput output = forGettingUserByToken.getUserByToken(token);

            if (output != null) {
                UserDetailsImpl userDetails = userOutputToUserDetails(output);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private UserDetailsImpl userOutputToUserDetails(GetUserByTokenOutput output) {
        return new UserDetailsImpl(
                output.userId().value(),
                output.name(),
                output.email(),
                output.login(),
                output.password(),
                output.address() != null ?
                        new AddressDetails(
                                output.address().addressId().value(),
                                output.address().street(),
                                output.address().number(),
                                output.address().complement(),
                                output.address().city(),
                                output.address().state(),
                                output.address().zipCode(),
                                output.address().createdAt(),
                                output.address().updatedAt()
                        ) : null,
                output.userType(),
                output.roles(),
                output.createdAt(),
                output.updatedAt()
        );
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            return null;
        }

        return authHeader.replace("Bearer ", "");
    }
}