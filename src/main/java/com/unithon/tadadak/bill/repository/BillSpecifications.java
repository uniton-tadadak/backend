package com.unithon.tadadak.bill.repository;

import com.unithon.tadadak.bill.domain.Bill;
import com.unithon.tadadak.bill.domain.BillStatus;
import org.springframework.data.jpa.domain.Specification;

public class BillSpecifications {
    public static Specification<Bill> hasGroupId(Long groupId) {
        return (root, q, cb) -> groupId == null ? null :
                cb.equal(root.get("group").get("groupId"), groupId);
    }
    public static Specification<Bill> hasUserId(Long userId) {
        return (root, q, cb) -> userId == null ? null :
                cb.equal(root.get("user").get("userId"), userId);
    }
    public static Specification<Bill> hasStatus(BillStatus status) {
        return (root, q, cb) -> status == null ? null :
                cb.equal(root.get("status"), status);
    }
}
