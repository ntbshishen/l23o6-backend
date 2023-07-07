package org.fffd.l23o6.mapper;

import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.vo.train.AdminTrainVO;
import org.fffd.l23o6.pojo.vo.train.TrainVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TrainMapper {
    TrainMapper INSTANCE = Mappers.getMapper(TrainMapper.class);

    @Mapping(source = "TrainEntity.trainType.text", target = "trainType")
    AdminTrainVO toAdminTrainVO(TrainEntity TrainEntity);

    @Mapping(source = "TrainEntity.trainType.text", target = "trainType")
    TrainVO toTrainVO(TrainEntity TrainEntity);
}

