package korn03.tradeguardserver.endpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmittedJobOrderLinkDTO {
    private int jobId;
    private String orderLinkId;
}
