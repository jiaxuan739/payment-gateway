package com.payment.gateway.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.payment.gateway.model.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaymentOrderRepository extends BaseMapper<PaymentOrder> {

    @Select("SELECT * FROM payment_order WHERE order_no = #{orderNo}")
    PaymentOrder findByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM payment_order WHERE trade_no = #{tradeNo}")
    PaymentOrder findByTradeNo(@Param("tradeNo") String tradeNo);

    /**
     * Find successful orders for a given channel on a given date, for reconciliation.
     */
    @Select("""
        SELECT * FROM payment_order
        WHERE channel = #{channel}
          AND status = 'SUCCESS'
          AND DATE(created_at) = #{billDate}
        ORDER BY id
    """)
    List<PaymentOrder> findSuccessOrdersByDate(@Param("channel") String channel,
                                                @Param("billDate") String billDate);
}
