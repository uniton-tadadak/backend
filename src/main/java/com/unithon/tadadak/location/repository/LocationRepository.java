package com.unithon.tadadak.location.repository;

import com.unithon.tadadak.location.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
