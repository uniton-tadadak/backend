package com.unithon.tadadak.bill.repository;

import com.unithon.tadadak.bill.domain.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {
}
