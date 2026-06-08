package com.miasi.helpdesk.application.domain.services;

import com.miasi.helpdesk.application.domain.model.Category;

public class NoAvailableAgentException extends RuntimeException {
  public NoAvailableAgentException(Category category) {
    super("No available agent for category: " + category.name());
  }
}
