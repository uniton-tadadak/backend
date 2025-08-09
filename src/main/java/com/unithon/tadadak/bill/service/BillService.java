package com.unithon.tadadak.bill.service;

import com.unithon.tadadak.bill.domain.Bill;
import com.unithon.tadadak.bill.dto.BillRequestDto;
import com.unithon.tadadak.bill.repository.BillRepository;
import com.unithon.tadadak.groups.repository.GroupsRepository;
import com.unithon.tadadak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BillService {
    private final BillRepository billRepository;
    private final GroupsRepository groupsRepository;
    private final UserRepository userRepository;

    public Bill create(BillRequestDto dto) {
        return billRepository.save(Bill.builder()
                .group(groupsRepository.findById(dto.getGroupId()).orElseThrow())
                .user(userRepository.findById(dto.getUserId()).orElseThrow())
                .amount(dto.getAmount())
                .status(dto.getStatus())
                .createdAt(LocalDateTime.now())
                .build());
    }
}

