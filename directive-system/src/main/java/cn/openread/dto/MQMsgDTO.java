package cn.openread.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MQMsgDTO {
    private String devId;
    private String directive;
}
