package com.champsoft.profiledashboardmanagement.DataAccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin,AdminId> {

    Admin findAdminByAdminId_uuid(String uuid);

    boolean existsByAdminId(AdminId adminId);
}
