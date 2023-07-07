package org.fffd.l23o6.service;

import java.util.List;

import org.fffd.l23o6.pojo.vo.order.OrderVO;

public interface OrderService {
    Long createOrder(String username, Long trainId, Long fromStationId, Long toStationId, String seatType, Long seatNumber,Long TruePrice);
    List<OrderVO> listOrders(String username);
    OrderVO getOrder(Long id);

    String cancelOrder(Long id);
    String payOrder(Long id,boolean credit);

}
