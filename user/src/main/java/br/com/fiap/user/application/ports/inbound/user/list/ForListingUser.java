package br.com.fiap.user.application.ports.inbound.user.list;

import br.com.fiap.user.application.domain.pagination.Pagination;
import br.com.fiap.user.application.ports.inbound.user.list.output.ListUserOutput;

public interface ForListingUser {

    Pagination<ListUserOutput> listUsers(int pageNumber, int pageSize);

}
