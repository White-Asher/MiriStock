package com.udteam.miristock.dto;

import com.udteam.miristock.entity.Deal;
import com.udteam.miristock.entity.LimitPriceOrderEntity;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LimitPriceOrderDto {
    private Integer limitPriceOrderNo;
    private String stockCode;
    private String stockName;
    private Integer memberNo;
    private Long limitPriceOrderPrice;
    private Long limitPriceOrderAmount;
    private Deal limitPriceOrderType;

    @Builder
    public LimitPriceOrderDto(LimitPriceOrderEntity entity) {
        this.limitPriceOrderNo = entity.getLimitPriceOrderNo();
        this.stockCode = entity.getStockCode();
        this.stockName = entity.getStockName();
        this.memberNo = entity.getMemberNo();
        this.limitPriceOrderPrice = entity.getLimitPriceOrderPrice();
        this.limitPriceOrderAmount = entity.getLimitPriceOrderAmount();
        this.limitPriceOrderType = entity.getLimitPriceOrderType();
    }

    public LimitPriceOrderEntity toEntity() {
        return LimitPriceOrderEntity.builder()
                .limitPriceOrderNo(limitPriceOrderNo)
                .stockCode(stockCode)
                .stockName(stockName)
                .memberNo(memberNo)
                .limitPriceOrderPrice(limitPriceOrderPrice)
                .limitPriceOrderAmount(limitPriceOrderAmount)
                .limitPriceOrderType(limitPriceOrderType)
                .build();
    }


}
