package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.EmailAddress;
import com.miasi.users.domain.model.UserID;

public interface ChangeEmailUseCase {
  void changeEmail(UserID id, EmailAddress newEmail);
}
