package org.fffd.l23o6.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alipay.api.AlipayApiException;
import jakarta.servlet.http.HttpServletResponse;
import org.fffd.l23o6.dao.OrderDao;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.dao.UserDao;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.fffd.l23o6.pojo.enum_.OrderStatus;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.vo.order.OrderVO;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;
import org.fffd.l23o6.service.OrderService;
import org.fffd.l23o6.util.strategy.payment.AlipaymentStrategy;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    public Long createOrder(String username, Long trainId, Long fromStationId, Long toStationId, String seatType,
            Long seatNumber,Long TruePrice) {
        UserEntity userEntity=userDao.findByUsername(username);
        Long userId = userEntity.getId();
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();

        int startStationIndex = route.getStationIds().indexOf(fromStationId);
        int endStationIndex = route.getStationIds().indexOf(toStationId);
        String seat = null;

        switch (train.getTrainType()) {
            case HIGH_SPEED:
                seat = GSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        GSeriesSeatStrategy.GSeriesSeatType.fromString(seatType), train.getSeats());
                break;
            case NORMAL_SPEED:
                seat = KSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        KSeriesSeatStrategy.KSeriesSeatType.fromString(seatType), train.getSeats());
                break;
        }
        if (seat == null) {
            throw new BizException(BizError.OUT_OF_SEAT);
        }


        OrderEntity order = OrderEntity.builder().trainId(trainId).userId(userId).seat(seat)
                .status(OrderStatus.PENDING_PAYMENT).arrivalStationId(toStationId).departureStationId(fromStationId).TruePrice(TruePrice)
                .build();
        System.out.println(order);
        train.setUpdatedAt(null);// force it to update
        trainDao.save(train);
        orderDao.save(order);
        //userEntity.setCredit(0);
        //userDao.save(userEntity);
        //System.out.println(order);
        return order.getId();
    }

    public List<OrderVO> listOrders(String username) {
        Long userId = userDao.findByUsername(username).getId();
        List<OrderEntity> orders = orderDao.findByUserId(userId);
        System.out.println("orders");
        System.out.println(orders);
        orders.sort((o1,o2)-> o2.getId().compareTo(o1.getId()));
        for(OrderEntity order:orders){
          //  if(trainDao.findById(order.getTrainId()))

        }
        return orders.stream().map(order -> {
            TrainEntity train = trainDao.findById(order.getTrainId()).get();
           // System.out.println(train);
            RouteEntity route = routeDao.findById(train.getRouteId()).get();
            int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
            int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
            Date date = new Date();System.out.println(date);
            Date arrivalTime=train.getArrivalTimes().get(endIndex);
            if(order.getStatus().getText().equals(OrderStatus.PAID.getText())&&date.after(arrivalTime)){
                    order.setStatus(OrderStatus.COMPLETED);
            }
            return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                    .seat(order.getSeat()).status(order.getStatus().getText())
                    .createdAt(order.getCreatedAt())
                    .startStationId(order.getDepartureStationId())
                    .endStationId(order.getArrivalStationId())
                    .departureTime(train.getDepartureTimes().get(startIndex))
                    .arrivalTime(arrivalTime)
                    .build();
        }).collect(Collectors.toList());

    }

    public OrderVO getOrder(Long id) {
       OrderEntity order = orderDao.findById(id).get();
        TrainEntity train = trainDao.findById(order.getTrainId()).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
        int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
        return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                .seat(order.getSeat()).status(order.getStatus().getText())
                .createdAt(order.getCreatedAt())
                .startStationId(order.getDepartureStationId())
                .endStationId(order.getArrivalStationId())
                .departureTime(train.getDepartureTimes().get(startIndex))
                .arrivalTime(train.getArrivalTimes().get(endIndex))
                .truePrice(order.getTruePrice())
                .build();

    }

    public String cancelOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        TrainEntity train=trainDao.findById(order.getTrainId()).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        String result="";
        int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
        int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
        boolean[][] resetseat = new boolean[endIndex-startIndex][];
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }
        switch (train.getTrainType()) {
            case HIGH_SPEED:
                String seat=order.getSeat();
                System.out.println(seat);
                resetseat=GSeriesSeatStrategy.INSTANCE.resetseat(seat,startIndex,endIndex,train.getSeats());
                System.out.println(train);
                break;
            case NORMAL_SPEED:

                break;
        }
        if(order.getStatus() == OrderStatus.PAID){
           // UserEntity user = userDao.findById(order.getUserId()).get();
           // user.setMoney(user.getMoney() + order.getTruePrice());
            // set the strategy is credit equals money(the strategy can be easily changed)
           // user.setCredit(user.getCredit() - order.getTruePrice());

            try {
                result= AlipaymentStrategy.INSTANCE.refund(order);
            } catch (AlipayApiException e) {
                throw new RuntimeException(e);
            }

        }
        // TODO: refund user's money and credits if needed

        order.setStatus(OrderStatus.CANCELLED);
        train.setSeats(resetseat);
        train.setUpdatedAt(null);
        orderDao.save(order);
        trainDao.save(train);
        return result;
    }

    public String payOrder(Long id,boolean credit) {
        OrderEntity order = orderDao.findById(id).get();
        UserEntity usr=userDao.findById(order.getUserId()).get();
        String result="";
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }
        try {
            long[] res=new long[3];
            if(credit){
             res=AlipaymentStrategy.INSTANCE.calOrderTruePrice(usr.getCredit(),order.getTruePrice());
             usr.setCredit(res[2]-res[1]);
             userDao.save(usr);
            }
            else{
                usr.setCredit(order.getTruePrice()+res[2]);
            }
            result= AlipaymentStrategy.INSTANCE.pay(order,credit,res[0]);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        if(result!=null){
            order.setStatus(OrderStatus.PAID);
        }
        orderDao.save(order);
        return result;
    }


}
