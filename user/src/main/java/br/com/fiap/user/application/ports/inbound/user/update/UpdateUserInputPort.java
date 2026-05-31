package br.com.fiap.user.application.ports.inbound.user.update;

import br.com.fiap.user.application.ports.inbound.user.update.input.UpdateUserInput;
import br.com.fiap.user.application.ports.inbound.user.update.output.UpdateUserOutput;

public interface UpdateUserInputPort {

    UpdateUserOutput updateUser(UpdateUserInput input);
}
