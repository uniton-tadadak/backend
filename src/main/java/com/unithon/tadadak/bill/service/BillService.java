package com.unithon.tadadak.bill.service;

import com.unithon.tadadak.bill.domain.Bill;
import com.unithon.tadadak.bill.domain.BillStatus;
import com.unithon.tadadak.bill.dto.BillResponse;
import com.unithon.tadadak.bill.dto.CreateBillRequest;
import com.unithon.tadadak.bill.dto.UpdateBillRequest;
import com.unithon.tadadak.bill.dto.UpdateBillStatusRequest;
import com.unithon.tadadak.bill.repository.BillRepository;
import com.unithon.tadadak.bill.repository.BillSpecifications;
import com.unithon.tadadak.global.exception.CustomException;
import com.unithon.tadadak.global.exception.ErrorCode;
import com.unithon.tadadak.groupmember.repository.GroupMemberRepository;
import com.unithon.tadadak.groups.domain.Groups;
import com.unithon.tadadak.groups.repository.GroupsRepository;
import com.unithon.tadadak.user.domain.User;
import com.unithon.tadadak.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillService {

    private final BillRepository billRepository;
    private final GroupsRepository groupsRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public BillResponse create(CreateBillRequest req) {
        Groups group = groupsRepository.findById(req.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        groupMemberRepository.findByGroupIdAndUserId(group.getGroupId(), user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_GROUP_MEMBER));

        if (req.getAmount() < 0) throw new CustomException(ErrorCode.INVALID_AMOUNT);

        Bill bill = Bill.builder()
                .group(group)
                .user(user)
                .amount(req.getAmount())
                .status(BillStatus.PENDING)
                .build();

        Bill saved = billRepository.save(bill);
        return toResponse(saved);
    }

    public BillResponse update(Long billId, UpdateBillRequest req) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new CustomException(ErrorCode.BILL_NOT_FOUND));

        if (req.getAmount() != null) {
            if (req.getAmount() < 0) throw new CustomException(ErrorCode.INVALID_AMOUNT);
            bill.setAmount(req.getAmount());
        }
        return toResponse(bill);
    }

    public BillResponse updateStatus(Long billId, UpdateBillStatusRequest req) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new CustomException(ErrorCode.BILL_NOT_FOUND));
        bill.setStatus(req.getStatus());
        return toResponse(bill);
    }

    public void delete(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new CustomException(ErrorCode.BILL_NOT_FOUND));
        billRepository.delete(bill);
    }

    @Transactional(readOnly = true)
    public BillResponse get(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new CustomException(ErrorCode.BILL_NOT_FOUND));
        return toResponse(bill);
    }

    @Transactional(readOnly = true)
    public Page<BillResponse> list(Long groupId, Long userId, BillStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // ✅ Spring Data JPA 3.5+: where(...) 사용 지양. allOf + 필터링으로 대체
        List<Specification<Bill>> specs = new ArrayList<>();
        if (groupId != null) specs.add(BillSpecifications.hasGroupId(groupId));
        if (userId != null) specs.add(BillSpecifications.hasUserId(userId));
        if (status != null) specs.add(BillSpecifications.hasStatus(status));

        Specification<Bill> spec = Specification.allOf(specs);
        return billRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private BillResponse toResponse(Bill b) {
        // amount는 응답 시 그룹원 수로 1/N한 값으로 반환
        long memberCount = groupMemberRepository.countByGroupId(b.getGroup().getGroupId());
        if (memberCount <= 0) throw new CustomException(ErrorCode.GROUP_MEMBER_COUNT_INVALID);
        int share = b.getAmount() / (int) memberCount; // 정수 나눗셈(내림). 잔여분 처리는 추후 정책 반영

        return BillResponse.builder()
                .billId(b.getBillId())
                .groupId(b.getGroup().getGroupId())
                .userId(b.getUser().getUserId())
                .amount(share)
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .build();
    }
}

