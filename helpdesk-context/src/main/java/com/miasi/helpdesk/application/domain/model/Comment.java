package com.miasi.helpdesk.application.domain.model;

import java.time.Instant;

public record Comment(String authorId, String content, Instant createdAt) {}
