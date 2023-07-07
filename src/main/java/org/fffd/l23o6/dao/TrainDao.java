package org.fffd.l23o6.dao;

import jakarta.validation.constraints.NotNull;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface TrainDao extends JpaRepository<TrainEntity, Long>{
    TrainEntity findByRouteIdAndDate(Long id, @NotNull String date);
}
