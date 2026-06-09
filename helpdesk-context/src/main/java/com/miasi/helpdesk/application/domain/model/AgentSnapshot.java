package com.miasi.helpdesk.application.domain.model;

public record AgentSnapshot(String agentId, int currentLoad, boolean available) {}
