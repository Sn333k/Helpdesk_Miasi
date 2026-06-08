package com.miasi.helpdesk.domain.model;

public record AgentSnapshot(String agentId, int currentLoad, boolean available) {}
