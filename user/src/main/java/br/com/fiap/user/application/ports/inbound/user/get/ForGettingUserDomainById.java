package br.com.fiap.user.application.ports.inbound.user.get;

import br.com.fiap.user.application.domain.user.User;

public interface ForGettingUserDomainById {
    User findUserDomainById(String userId);
}
