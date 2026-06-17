package com.miasi.users.domain.model;

/** Snapshot of an agent's availability — used as a cross-context read model. */
public record AgentSnapshot(String agentId, String teamId, boolean available) {}
