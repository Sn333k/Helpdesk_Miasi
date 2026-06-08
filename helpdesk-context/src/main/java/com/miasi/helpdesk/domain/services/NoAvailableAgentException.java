package com.miasi.helpdesk.domain.services;

import com.miasi.helpdesk.domain.model.Category;

public class NoAvailableAgentException extends RuntimeException {
  public NoAvailableAgentException(Category category) {
    super("No available agent for category: " + category.name());
  }
}
