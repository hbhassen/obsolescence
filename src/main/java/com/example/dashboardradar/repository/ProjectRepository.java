package com.example.dashboardradar.repository;

import com.example.dashboardradar.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {
}
