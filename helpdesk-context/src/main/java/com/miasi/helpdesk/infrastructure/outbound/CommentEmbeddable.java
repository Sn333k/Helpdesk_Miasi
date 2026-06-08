package com.miasi.helpdesk.infrastructure.outbound;

import com.miasi.helpdesk.domain.model.Comment;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class CommentEmbeddable {

  private String authorId;
  private String content;
  private Instant createdAt;

  public CommentEmbeddable() {}

  CommentEmbeddable(Comment comment) {
    this.authorId = comment.authorId();
    this.content = comment.content();
    this.createdAt = comment.createdAt();
  }

  Comment toDomain() {
    return new Comment(authorId, content, createdAt);
  }
}
