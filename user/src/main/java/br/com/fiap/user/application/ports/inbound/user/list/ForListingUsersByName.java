package br.com.fiap.user.application.ports.inbound.user.list;

import br.com.fiap.user.application.domain.pagination.Pagination;
import br.com.fiap.user.application.ports.inbound.user.list.output.ListUserOutput;

public interface ForListingUsersByName {
    Pagination<ListUserOutput> findAllByName(int pageNumber, int pageSize, String name);
}
