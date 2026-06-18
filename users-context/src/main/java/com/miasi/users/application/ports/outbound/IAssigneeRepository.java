package com.miasi.users.application.ports.outbound;

import com.miasi.users.domain.model.Assignee;
import com.miasi.users.domain.model.UserID;
import java.util.List;
import java.util.Optional;

public interface IAssigneeRepository {
  void save(Assignee assignee);

  Optional<Assignee> findById(UserID id);

  List<Assignee> findAvailableByArea(String area);
}
