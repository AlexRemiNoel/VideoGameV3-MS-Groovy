package com.example.videogamev3.DownloadManagement.DataAccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadRepository extends JpaRepository<Download, String> {
    Download findDownloadById_Uuid(String id);

    boolean existsDownloadById_Uuid(String existingId);

    void deleteDownloadById_Uuid(String idToDelete);
}