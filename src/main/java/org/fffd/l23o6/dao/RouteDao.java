package org.fffd.l23o6.dao;

import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteDao extends JpaRepository<RouteEntity, Long>{

}
