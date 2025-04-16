package com.champsoft.downloadmanagement.DataAccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID; // Changed from DownloadId

@Repository
// ID type is now UUID
public interface DownloadManagerRepository extends JpaRepository<DownloadManager, UUID> {
    // Standard CRUD methods provided by Spring Data JPA
}