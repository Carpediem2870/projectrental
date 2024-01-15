package com.team5.projrental.payment;

import com.team5.projrental.common.Flag;
import com.team5.projrental.common.Role;
import com.team5.projrental.common.exception.*;
import com.team5.projrental.common.model.ResVo;
import com.team5.projrental.common.utils.CommonUtils;
import com.team5.projrental.payment.model.PaymentInsDto;
import com.team5.projrental.payment.model.PaymentListVo;
import com.team5.projrental.payment.model.PaymentVo;
import com.team5.projrental.payment.model.proc.DelPaymentDto;
import com.team5.projrental.payment.model.proc.GetInfoForCheckIproductAndIuserResult;
import com.team5.projrental.payment.model.proc.GetPaymentListDto;
import com.team5.projrental.payment.model.proc.GetPaymentListResultDto;
import com.team5.projrental.product.ProductRepository;
import com.team5.projrental.product.model.innermodel.StoredFileInfo;
import com.team5.projrental.security.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.team5.projrental.common.Const.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    /* TODO: 1/11/24
        1. 페이징,
        2. istatus -4 (기간 지남) 추가된것 처리 (기간지남과 후기2개가 등록되어 만료됨을 구분),
        3. 스케쥴러로 매일 자정에 기간지남으로 변경하는것 (rental_end_date 를 기준으로) 구현
        --by Hyunmin */
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final AuthenticationFacade authenticationFacade;

    @Transactional
    public ResVo postPayment(PaymentInsDto paymentInsDto) {

        // 1일당 가격 가져오기
        int rentalPrice = productRepository.findRentalPriceBy(paymentInsDto.getIproduct());
        // request 데이터 검증
        if (rentalPrice == 0) {
            throw new NoSuchProductException(NO_SUCH_PRODUCT_EX_MESSAGE);
        }
        paymentInsDto.setIbuyer(authenticationFacade.getLoginUserPk());
        CommonUtils.ifFalseThrow(NoSuchUserException.class, NO_SUCH_USER_EX_MESSAGE,
                productRepository.findIuserCountBy(paymentInsDto.getIbuyer()));
        CommonUtils.ifBeforeThrow(BadDateInfoException.class, RENTAL_END_DATE_MUST_BE_AFTER_THAN_RENTAL_START_DATE_EX_MESSAGE,
                paymentInsDto.getRentalEndDate(), paymentInsDto.getRentalStartDate());


        // 데이터 세팅 (+마지막 예외처리)
        paymentInsDto.setIpaymentMethod(CommonUtils.ifPaymentMethodNotContainsThrowOrReturn(paymentInsDto.getPaymentMethod()));
        paymentInsDto.setRentalDuration(((int) ChronoUnit.DAYS.between(paymentInsDto.getRentalStartDate(),
                paymentInsDto.getRentalEndDate())) + 1);
        paymentInsDto.setPrice(rentalPrice * paymentInsDto.getRentalDuration());
        paymentInsDto.setCode(createCode());
        paymentInsDto.setDeposit(CommonUtils.getDepositFromPer(paymentInsDto.getPrice(), paymentInsDto.getDepositPer()));

        // insert (select key 사용)
//        if (paymentRepository.savePayment(paymentInsDto) != 0 &&
//                productRepository.updateIpayment(paymentInsDto.getIproduct(), paymentInsDto.getIpayment()) != 0) {
//            if (paymentRepository.saveProductPayment(paymentInsDto.getIproduct(), paymentInsDto.getIpayment()) != 0) {
//                return new ResVo(1);
//            }
//        }
        if (paymentRepository.savePayment(paymentInsDto) != 0) {
            if (paymentRepository.saveProductPayment(paymentInsDto.getIproduct(), paymentInsDto.getIpayment()) != 0) {
                return new ResVo(1);
            }
        }

        throw new RuntimeException(SERVER_ERR_MESSAGE);

    }

    /**
     * div == 3
     * -3: ex (이미 완료된 거래)
     * -2: ex (이미 완료된 거래)
     * -1: ex (이미 완료된 거래)
     * 0: do (2 or 3)
     * 1: ex (이미 완료된 거래)
     * 2: do or ex (요청자가 구매자인 경우만 -3)
     * 3: do or ex (요청자가 판매자인 경우만 -3)
     * <p>
     * div == 2
     * -3: do
     * -2: ex (이미 숨김처리 됨)
     * -1: ex (삭제된 상품을 숨길 수 없음)
     * 0: ex (거래중이면 숨길수 없음)
     * 1: do
     * 2: ex (거래중이면 숨길수 없음)
     * 3: ex (거래중이면 숨길수 없음)
     * <p>
     * div == 1
     * -3: do
     * -2: do
     * -1: ex (이미 삭제된 결제)
     * 0: ex (거래중이면 삭제 불가능)
     * 1: do
     * 2: ex (거래중이면 삭제 불가능)
     * 3: ex (거래중이면 삭제 불가능)
     *
     * @param ipayment
     * @param div
     * @return ResVo
     */
    public ResVo delPayment(Integer ipayment, Integer div) {

        // 데이터 검증
//        CommonUtils.ifFalseThrow(NoSuchProductException.class, NO_SUCH_PRODUCT_EX_MESSAGE,
//                productRepository.findIproductCountBy(iproduct));
//        CommonUtils.ifFalseThrow(NoSuchUserException.class, NO_SUCH_USER_EX_MESSAGE,
//                productRepository.findIuserCountBy(iuser));

        GetInfoForCheckIproductAndIuserResult checkResult = paymentRepository.checkIuserAndIproduct(ipayment);
        if (checkResult == null) {
            throw new NoSuchProductException(NO_SUCH_PRODUCT_EX_MESSAGE);
        }
        int iuser = authenticationFacade.getLoginUserPk();
        if (checkResult.getISeller() != iuser && checkResult.getIBuyer() != iuser) {
            throw new NoSuchUserException(NO_SUCH_USER_EX_MESSAGE);
        }
        // 취소요청이 아니면 오늘이 반납일보다 이후여야만 함.
        // 오늘이 반납일보다 이후가 아닌데 (대여 중인데) 삭제나 숨김 요청을 한 경우
        if (div != 3 && !LocalDate.now().isAfter(checkResult.getRentalEndDate())) {
            throw new BadDateInfoException(BAD_RENTAL_DEL_EX_MESSAGE);
        }
        // 반납일이 오늘보다 이후인데 취소요청을 한 경우
        if (div == 3 && LocalDate.now().isAfter(checkResult.getRentalEndDate())) {
            throw new BadDateInfoException(BAD_RENTAL_CANCEL_EX_MESSAGE);
        }


        // 변경할 istatus 생성
        Integer istatusForUpdate = divResolver(div, checkResult.getIstatus(), iuser == checkResult.getIBuyer() ? Role.BUYER :
                iuser == checkResult.getISeller() ? Role.SELLER : null);
        // 객체 생성
        DelPaymentDto delPaymentDto = new DelPaymentDto(ipayment, istatusForUpdate);

        if (paymentRepository.deletePayment(delPaymentDto) != 0) {
            throw new RuntimeException(SERVER_ERR_MESSAGE);
        }
        return new ResVo(istatusForUpdate);
    }

    public List<PaymentListVo> getAllPayment(Integer role) {
        int iuser = authenticationFacade.getLoginUserPk();
        List<GetPaymentListResultDto> paymentBy = paymentRepository.findPaymentBy(new GetPaymentListDto(iuser, role));
        List<PaymentListVo> result = new ArrayList<>();
        CommonUtils.checkNullOrZeroIfCollectionThrow(NoSuchPaymentException.class, NO_SUCH_PAYMENT_EX_MESSAGE, paymentBy);
        paymentBy.forEach(p -> new PaymentListVo(
                p.getNick(), CommonUtils.getPic(new StoredFileInfo(p.getRequestPic(), p.getStoredPic())),
                p.getIpayment(), p.getIproduct(), STATUS.get(p.getIstatus()), p.getRentalStartDate(), p.getRentalEndDate(),
                p.getRentalDuration(), p.getPrice(), p.getDeposit()
        ));
        return result;
    }


    public PaymentVo getPayment(Integer ipayment) {
        // iuser 또는 ipayment 가 없으면 결과가 size 0 일 것

        // 가져오기
        GetPaymentListResultDto aPayment;
        int iuser = authenticationFacade.getLoginUserPk();
        try {
            aPayment = paymentRepository.findPaymentBy(new GetPaymentListDto(iuser, ipayment,
                    Flag.ONE.getValue())).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchPaymentException(NO_SUCH_PAYMENT_EX_MESSAGE);
        }

        return new PaymentVo(
                aPayment.getNick(),
                CommonUtils.getPic(new StoredFileInfo(aPayment.getRequestPic(), aPayment.getStoredPic())),
                aPayment.getIpayment(),
                aPayment.getIproduct(),
                STATUS.get(aPayment.getIstatus()), // status resolver ?
                aPayment.getRentalStartDate(),
                aPayment.getRentalEndDate(),
                aPayment.getRentalDuration(),
                aPayment.getPrice(),
                aPayment.getDeposit(),
                aPayment.getPhone(),
                PAYMENT_METHODS.get(aPayment.getIpayment()),
                aPayment.getCode(),
                aPayment.getCreatedAt()
        );
    }

    /*
    ------- Extracted Method -------
    */
    private String createCode() {

        String systemCurrent = String.valueOf(System.currentTimeMillis());
        String uuidFront = UUID.randomUUID().toString().substring(0, 4);
        String uuidBackBase = UUID.randomUUID().toString();
        String uuidBack = uuidBackBase.substring(uuidBackBase.length() - 3);

        return new StringBuilder().append(uuidFront).append(systemCurrent).append(uuidBack).toString();

    }

    private Integer divResolver(Integer div, Integer istatus, Role role) {
        if (role == null) throw new BadInformationException(BAD_INFO_EX_MESSAGE);
        if (div == 1 || div == 2) {
            if (istatus == -3 || istatus == 1 || (div == 1 && istatus == -2)) {
                return div * -1;
            }
        }
        if (div == 3) {
            if (istatus == 0) {
                if (role == Role.BUYER) {
                    return 2;
                }
                if (role == Role.SELLER) {
                    return 3;
                }
            }
            if (istatus == 2 && role == Role.BUYER || istatus == 3 && role == Role.SELLER) {
                return div * -1;
            }
        }
        throw new BadDivInformationException(BAD_DIV_INFO_EX_MESSAGE);
    }


}
