package com.champsoft.downloadmanagement.DataAccess;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor; // Required by JPA for Embeddable

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor // JPA requirement
@AllArgsConstructor // Convenience
@EqualsAndHashCode // Essential for Value Objects
public class DownloadId implements Serializable { // Serializable needed for EmbeddedId
    private UUID uuid;

    public static DownloadId generate() {
        return new DownloadId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}