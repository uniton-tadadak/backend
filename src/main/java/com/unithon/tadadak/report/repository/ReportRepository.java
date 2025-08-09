package com.unithon.tadadak.report.repository;

import com.unithon.tadadak.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
