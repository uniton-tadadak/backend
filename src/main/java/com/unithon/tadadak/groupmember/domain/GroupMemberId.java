package com.unithon.tadadak.groupmember.domain;

import lombok.*;
import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class GroupMemberId implements Serializable {
    private Long group;  // GroupMember.group.groupId와 매핑
    private Long user;   // GroupMember.user.userId와 매핑
}
