package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.UserID;

public interface CreateRequesterUseCase {
  UserID createRequester(String email);
}
