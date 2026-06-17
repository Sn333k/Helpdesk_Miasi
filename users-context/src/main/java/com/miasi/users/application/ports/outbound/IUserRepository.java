package com.miasi.users.application.ports.outbound;

import com.miasi.users.domain.model.User;
import com.miasi.users.domain.model.UserID;
import java.util.List;
import java.util.Optional;

public interface IUserRepository {
  void save(User user);

  Optional<User> findById(UserID id);

  List<User> findAvailableAgents();
}
