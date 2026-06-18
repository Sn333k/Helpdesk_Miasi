package com.miasi.users.infrastructure.persistence;

import com.miasi.users.domain.model.AccountStatus;
import com.miasi.users.domain.model.EmailAddress;
import com.miasi.users.domain.model.Requester;
import com.miasi.users.domain.model.UserID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "requesters")
public class RequesterEntity {

  @Id private String id;
  private String email;
  private String accountStatus;

  public RequesterEntity() {}

  public RequesterEntity(Requester requester) {
    this.id = requester.getId().id();
    this.email = requester.getEmail().value();
    this.accountStatus = requester.getAccountStatus().name();
  }

  public Requester toDomain() {
    return Requester.reconstitute(
        new UserID(id), new EmailAddress(email), AccountStatus.valueOf(accountStatus));
  }
}
