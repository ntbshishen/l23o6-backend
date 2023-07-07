package org.fffd.l23o6.pojo.vo.station;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StationVO {
    private Long id;
    private String name;
}
