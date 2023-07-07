package org.fffd.l23o6.util.strategy.payment;

import com.alipay.api.AlipayApiException;
import jakarta.servlet.http.HttpServletResponse;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.springframework.data.domain.Sort;

public abstract class PaymentStrategy {

    public String pay(final OrderEntity order,boolean credit,Long creditPrice) throws AlipayApiException {return null;}
    public String refund(OrderEntity order) throws AlipayApiException{return null;}
}
