package com.fourt.railskylines.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourt.railskylines.domain.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>,
                JpaSpecificationExecutor<Permission> {
        boolean existsByModuleAndApiPathAndMethod(String model, String apiPath, String method);

        List<Permission> findByIdIn(List<Long> list);
}
