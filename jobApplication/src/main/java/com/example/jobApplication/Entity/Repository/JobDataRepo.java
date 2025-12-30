package com.example.jobApplication.Entity.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.jobApplication.Entity.JobData;

public interface JobDataRepo extends JpaRepository<JobData, Long> {

}
