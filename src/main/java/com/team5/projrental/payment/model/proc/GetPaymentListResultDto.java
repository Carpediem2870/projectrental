package com.team5.projrental.payment.model.proc;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GetPaymentListResultDto {
    private Integer iuser;
    private String nick;
    private String storedPic;
    private String requestPic;

    private Integer ipayment;
    private Integer iproduct;
    private Integer istatus;
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private Integer rentalDuration;
    private Integer price;
    private Integer deposit;
    // for a payment
    private String phone;
    private String code;
    private LocalDateTime createdAt;
}