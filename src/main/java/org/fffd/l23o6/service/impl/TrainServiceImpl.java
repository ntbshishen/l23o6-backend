package org.fffd.l23o6.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.mapper.TrainMapper;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.enum_.TrainType;
import org.fffd.l23o6.pojo.vo.train.AdminTrainVO;
import org.fffd.l23o6.pojo.vo.train.TrainVO;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;
import org.fffd.l23o6.pojo.vo.train.TrainDetailVO;
import org.fffd.l23o6.service.TrainService;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import io.github.lyc8503.spring.starter.incantation.exception.CommonErrorType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    @Override
    public TrainDetailVO getTrain(Long trainId) {
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        if(train.getStationId().size()==0){
            train.setStationId(route.getStationIds());
            trainDao.save(train);
        }
        return TrainDetailVO.builder().id(trainId).date(train.getDate()).name(train.getName())
                .stationIds(route.getStationIds()).arrivalTimes(train.getArrivalTimes())
                .departureTimes(train.getDepartureTimes()).extraInfos(train.getExtraInfos()).build();
    }

    @Override
    public List<TrainVO> listTrains(Long startStationId, Long endStationId, String date) {
        //用查找路线的方式会产生一个bug
        /**
         * bug：在后台修改路线时，会导致同时修改车次信息
         * 解决：在每个trainEntity中添加一个list<Long> stationid 信息，用来记录每趟路线信息。
         */
      //  List<RouteEntity> route=routeDao.findAll();
        List<TrainEntity> trainEntities=trainDao.findAll();

        List<TrainVO> train_list=new ArrayList<>();
        for(TrainEntity train_one:trainEntities){
            List<Long> temp=train_one.getStationId();
            if(temp.contains(startStationId)&&temp.contains(endStationId)&&train_one.getDate().equals(date)){
                List<TicketInfo> ticketInfos=new ArrayList<>();
              //  Long route_oneId=train_one.getRouteId();
                int startStationIndex = train_one.getStationId().indexOf(startStationId);
                int endStationIndex = train_one.getStationId().indexOf(endStationId);
                //System.out.println(startStationIndex+":"+endStationIndex);
                //TrainEntity trainEntity=trainDao.findByRouteIdAndDate(route_oneId,date);
                //if(trainEn==null){
                  //  throw new BizException(CommonErrorType.NOT_FOUND,"日期不符合");
              //  }
                TrainVO trainVO=TrainMapper.INSTANCE.toTrainVO(train_one);
                switch (train_one.getTrainType()) {
                    case HIGH_SPEED:
                        Map<GSeriesSeatStrategy.GSeriesSeatType,Integer> Gticketlist=GSeriesSeatStrategy.INSTANCE.getLeftSeatCount(startStationIndex,endStationIndex,train_one.getSeats());
                        for (Map.Entry<GSeriesSeatStrategy.GSeriesSeatType, Integer> ticket : Gticketlist.entrySet()){
                            TicketInfo ticketInfo=new TicketInfo(ticket.getKey().getText(),ticket.getValue(),GSeriesSeatStrategy.INSTANCE.getTicketPrice(ticket.getKey().getText()));
                            ticketInfos.add(ticketInfo);
                        }
                        break;
                    case NORMAL_SPEED:
                        Map<KSeriesSeatStrategy.KSeriesSeatType,Integer> Kticketlist=KSeriesSeatStrategy.INSTANCE.getLeftSeatCount(startStationIndex,endStationIndex,train_one.getSeats());
                        for (Map.Entry<KSeriesSeatStrategy.KSeriesSeatType,Integer> ticket : Kticketlist.entrySet()){
                            TicketInfo ticketInfo=new TicketInfo(ticket.getKey().getText(),ticket.getValue(),100);
                            ticketInfos.add(ticketInfo);
                        }
                        break;
                }
                trainVO.setStartStationId(startStationId);
                trainVO.setEndStationId(endStationId);
                trainVO.setDepartureTime(train_one.getDepartureTimes().get(startStationIndex));
                trainVO.setArrivalTime(train_one.getArrivalTimes().get(endStationIndex));
                trainVO.setTicketInfo(ticketInfos);
                //System.out.println(trainVO);
                //System.out.println();
                train_list.add(trainVO);
            }
        }
      /*  for(RouteEntity route_one:route){

            List<Long> temp=route_one.getStationIds();
            if(temp.contains(startStationId)&&temp.contains(endStationId)){
                List<TicketInfo> ticketInfos=new ArrayList<>();
                Long route_oneId=route_one.getId();
                int startStationIndex = route_one.getStationIds().indexOf(startStationId);
                int endStationIndex = route_one.getStationIds().indexOf(endStationId);
                System.out.println(startStationIndex+":"+endStationIndex);
                TrainEntity trainEntity=trainDao.findByRouteIdAndDate(route_oneId,date);
                if(trainEntity==null){
                    throw new BizException(CommonErrorType.NOT_FOUND,"日期不符合");
                }
                TrainVO trainVO=TrainMapper.INSTANCE.toTrainVO(trainEntity);
                switch (trainEntity.getTrainType()) {
                    case HIGH_SPEED:
                        Map<GSeriesSeatStrategy.GSeriesSeatType,Integer> Gticketlist=GSeriesSeatStrategy.INSTANCE.getLeftSeatCount(startStationIndex,endStationIndex,trainEntity.getSeats());
                        for (Map.Entry<GSeriesSeatStrategy.GSeriesSeatType, Integer> ticket : Gticketlist.entrySet()){
                            TicketInfo ticketInfo=new TicketInfo(ticket.getKey().getText(),ticket.getValue(),GSeriesSeatStrategy.INSTANCE.getTicketPrice(ticket.getKey().getText()));
                            ticketInfos.add(ticketInfo);
                        }
                            break;
                    case NORMAL_SPEED:
                        Map<KSeriesSeatStrategy.KSeriesSeatType,Integer> Kticketlist=KSeriesSeatStrategy.INSTANCE.getLeftSeatCount(startStationIndex,endStationIndex,trainEntity.getSeats());
                        for (Map.Entry<KSeriesSeatStrategy.KSeriesSeatType,Integer> ticket : Kticketlist.entrySet()){
                            TicketInfo ticketInfo=new TicketInfo(ticket.getKey().getText(),ticket.getValue(),100);
                            ticketInfos.add(ticketInfo);
                        }
                        break;
                }
                trainVO.setStartStationId(startStationId);
                trainVO.setEndStationId(endStationId);
                trainVO.setDepartureTime(trainEntity.getDepartureTimes().get(startStationIndex));
                trainVO.setArrivalTime(trainEntity.getArrivalTimes().get(endStationIndex));
                trainVO.setTicketInfo(ticketInfos);
                System.out.println(trainVO);
                System.out.println(trainEntity);
                train_list.add(trainVO);
            }
        }*/
        if(train_list.size()==0)
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS,"车次不存在");
        return train_list;

    }

    @Override
    public List<AdminTrainVO> listTrainsAdmin() {
        System.out.println(trainDao.findAll());
        return trainDao.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(TrainMapper.INSTANCE::toAdminTrainVO).collect(Collectors.toList());
    }

    @Override
    public void addTrain(String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
            List<Date> departureTimes) {
        RouteEntity route = routeDao.findById(routeId).get();
        TrainEntity entity = TrainEntity.builder().name(name).routeId(routeId).trainType(type)
                .date(date).arrivalTimes(arrivalTimes).departureTimes(departureTimes).stationId(route.getStationIds()).build();
        if (route.getStationIds().size() != entity.getArrivalTimes().size()
                || route.getStationIds().size() != entity.getDepartureTimes().size()) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "列表长度错误");
        }
        entity.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        switch (entity.getTrainType()) {
            case HIGH_SPEED:
                entity.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
            case NORMAL_SPEED:
                entity.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
        }
        trainDao.save(entity);
    }

    @Override
    public void changeTrain(Long id, String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
                            List<Date> departureTimes) {

        TrainEntity entity=trainDao.findById(id).get();
        entity.setName(name).setRouteId(routeId).setTrainType(type).setDate(date).setArrivalTimes(arrivalTimes).setDepartureTimes(departureTimes);
        trainDao.save(entity);
    }

    @Override
    public void deleteTrain(Long id) {
        trainDao.deleteById(id);
    }
}
