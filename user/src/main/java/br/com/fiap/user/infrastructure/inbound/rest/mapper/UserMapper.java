package br.com.fiap.user.infrastructure.inbound.rest.mapper;

import br.com.fiap.user.application.ports.inbound.user.create.input.CreateUserInput;
import br.com.fiap.user.application.ports.inbound.user.create.output.CreateUserOutput;
import br.com.fiap.user.application.ports.inbound.user.get.output.GetUserByIdOutput;
import br.com.fiap.user.application.ports.inbound.user.list.output.ListUserOutput;
import br.com.fiap.user.application.ports.inbound.user.list.output.ListUsersByNameOutput;
import br.com.fiap.user.application.ports.inbound.user.password.input.UpdatePasswordInput;
import br.com.fiap.user.application.ports.inbound.user.password.output.UpdatePasswordOutput;
import br.com.fiap.user.application.ports.inbound.user.update.input.UpdateUserInput;
import br.com.fiap.user.application.ports.inbound.user.update.output.UpdateUserOutput;
import br.com.fiap.user.application.domain.user.address.AddressId;
import br.com.fiap.user.application.domain.user.UserId;
import br.com.fiap.user.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(CreateUserOutput output);
    UserDTO toDTO(UpdatePasswordOutput output);
    UserDTO toDTO(UpdateUserOutput output);
    UserDTO toDTO(GetUserByIdOutput output);
    List<UserDTO> toListDTO(List<ListUsersByNameOutput> output);
    UserDTO toDTO(ListUserOutput output);
    CreateUserInput fromDTO(CreateUserDTO dto);

    @Mapping(target = "userId", source = "userId")
    UpdateUserInput fromUpdateDTO(UpdateUserDTO dto, UUID userId);

    @Mapping(target = "userId", source = "userId")
    UpdatePasswordInput fromUpdatePasswordDTO(UpdatePasswordDTO updatePasswordDto, UUID userId);

    default String map(UserId value) {
        return value != null ? value.toString() : null;
    }

    default UserId mapUserId(String value) {
        return value != null ? UserId.from(value) : null;
    }

    default String map(AddressId value) {
        return value != null ? value.toString() : null;
    }

    default AddressId mapAddressId(String value) {
        return value != null ? AddressId.from(value) : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.of("-03:00")) : null;
    }
}
