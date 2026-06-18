package com.miasi.users.application.ports.outbound;

import com.miasi.users.domain.model.Requester;
import com.miasi.users.domain.model.UserID;
import java.util.Optional;

public interface IRequesterRepository {
  void save(Requester requester);

  Optional<Requester> findById(UserID id);
}
