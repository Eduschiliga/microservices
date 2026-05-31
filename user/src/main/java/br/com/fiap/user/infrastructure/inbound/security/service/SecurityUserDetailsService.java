package br.com.fiap.user.infrastructure.inbound.security.service;

import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.ports.outbound.repository.UserRepositoryPort;
import br.com.fiap.user.application.domain.user.address.Address;
import br.com.fiap.user.infrastructure.inbound.security.model.AddressDetails;
import br.com.fiap.user.infrastructure.inbound.security.model.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserDetailsService implements UserDetailsService {
    private final UserRepositoryPort userRepositoryPort;

    public SecurityUserDetailsService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepositoryPort.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new UserDetailsImpl(
                user.getUserId().value(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getPassword(),
                mapAddress(user.getAddress()),
                user.getUserType(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private AddressDetails mapAddress(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressDetails(
                address.getAddressId() != null ? address.getAddressId().value() : null,
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}