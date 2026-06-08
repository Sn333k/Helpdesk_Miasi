package com.miasi.helpdesk.domain.model;

import java.time.Instant;

public record Comment(String authorId, String content, Instant createdAt) {}
