//package korn03.tradeguardserver.kafka.events;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.experimental.SuperBuilder;
//
//import java.util.List;
//
//@Data
//@SuperBuilder
//@NoArgsConstructor
//@AllArgsConstructor
//public class JobSubmissionMessage {
//    private String source;
//    private Integer jobId;
//    private Long userId;
//    private JobEventType jobEventType;
//
//
//    private Integer totalSteps;
//    private List<String> coins;
//    private String side;
//    private Boolean force;
//    private Double discountPct;
//    private Double randomnessPct;
//
//    //optional
//    private List<String> excludeCoins;
//    private Double proportionPct;
//    private Double totalAmt;
//
//    private Long timestamp;
//}