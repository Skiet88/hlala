package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Mutual, sealed review - sealed flips to false only once BOTH sides have posted for the lease. */
@Entity
@Table(name = "review")
public class Review {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID leaseId;

    @Column(nullable = false)
    private UUID authorId;

    @Column(nullable = false)
    private UUID aboutUserId;

    @Column(nullable = false)
    private int stars;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "review_tag", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private boolean sealed;

    @Column(nullable = false, updatable = false)
    private Instant postedAt;

    protected Review() {
    }

    public Review(UUID id, UUID leaseId, UUID authorId, UUID aboutUserId, int stars, Set<String> tags,
                   String comment, boolean sealed, Instant postedAt) {
        this.id = id;
        this.leaseId = leaseId;
        this.authorId = authorId;
        this.aboutUserId = aboutUserId;
        this.stars = stars;
        this.tags = tags;
        this.comment = comment;
        this.sealed = sealed;
        this.postedAt = postedAt;
    }

    public Review unsealed() {
        return new Review(id, leaseId, authorId, aboutUserId, stars, Set.copyOf(tags), comment, false, postedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeaseId() {
        return leaseId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getAboutUserId() {
        return aboutUserId;
    }

    public int getStars() {
        return stars;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getComment() {
        return comment;
    }

    public boolean isSealed() {
        return sealed;
    }

    public Instant getPostedAt() {
        return postedAt;
    }
}
