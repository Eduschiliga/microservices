package br.com.fiap.user.application.usecases.auth;

import br.com.fiap.user.application.domain.exceptions.TokenInvalidException;
import br.com.fiap.user.application.domain.exceptions.UserNotFoundException;
import br.com.fiap.user.application.domain.exceptions.UserOrPasswordInvalidException;
import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.ports.inbound.auth.ForAuthenticateUser;
import br.com.fiap.user.application.ports.inbound.auth.ForGettingUserByToken;
import br.com.fiap.user.application.ports.inbound.auth.ForValidateToken;
import br.com.fiap.user.application.ports.inbound.auth.input.AuthenticateUserInput;
import br.com.fiap.user.application.ports.inbound.auth.output.AuthenticateUserOutput;
import br.com.fiap.user.application.ports.inbound.auth.output.GetUserByTokenOutput;
import br.com.fiap.user.application.ports.outbound.password.PasswordEncoderPort;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import br.com.fiap.user.application.ports.outbound.token.TokenGeneratorPort;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Objects;

@Named
public class AuthenticateUserUseCase implements
        ForAuthenticateUser,
        ForGettingUserByToken,
        ForValidateToken
{
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGeneratorPort;

    @Inject
    public AuthenticateUserUseCase(
            final UserRepositoryPort userRepositoryPort,
            final PasswordEncoderPort passwordEncoder,
            final TokenGeneratorPort tokenGeneratorPort
    ) {
        this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.tokenGeneratorPort = Objects.requireNonNull(tokenGeneratorPort);
    }

    @Override
    public AuthenticateUserOutput login(AuthenticateUserInput input) {
        User user = userRepositoryPort.findByLogin(input.login())
                .orElseThrow(() -> new UserOrPasswordInvalidException("Invalid username or password"));

        if (!passwordEncoder.matches(input.password(), user.getPassword())) {
            throw new UserOrPasswordInvalidException("Invalid username or password");
        }

        String token = tokenGeneratorPort.generate(user);

        return new AuthenticateUserOutput(token);
    }

    @Override
    public void validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new TokenInvalidException("Token null or empty");
        }

        if (tokenGeneratorPort.isExpiredToken(token)) {
            throw new TokenInvalidException("Token expired, please generate another valid token.");
        }
    }

    @Override
    public GetUserByTokenOutput getUserByToken(String token) {
        String login = tokenGeneratorPort.getSubjectByToken(token);

        if (login == null || login.isEmpty()) {
            throw new TokenInvalidException("Login Invalid");
        }

        User user = userRepositoryPort.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found with the provided token."));

        return GetUserByTokenOutput.from(user);
    }
}
