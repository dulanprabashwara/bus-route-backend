package lk.busroute.dto;

import java.math.BigDecimal;

public class FareResultDto {
    private BigDecimal amount;
    private String fareType; // EXACT_POINT_TO_POINT, FULL_ENDPOINT_ONLY, FARE_UNAVAILABLE
    private String status;   // EXACT, ENDPOINT_ONLY, UNAVAILABLE
    private String source;

    public FareResultDto() {}

    public FareResultDto(BigDecimal amount, String fareType, String status, String source) {
        this.amount = amount;
        this.fareType = fareType;
        this.status = status;
        this.source = source;
    }

    public static FareResultDto unavailable() {
        return new FareResultDto(null, "FARE_UNAVAILABLE", "UNAVAILABLE", "NTC_NO_DATA");
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getFareType() { return fareType; }
    public void setFareType(String fareType) { this.fareType = fareType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
