package org.fffd.l23o6.clear;

import org.fffd.l23o6.dao.OrderDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



@Component
public class clearOrder {
    @Autowired
    private OrderDao orderDao;
    @Scheduled(cron = "0 30 9 1 * ?")
    private void clearData() {
        try {
            //System.err.println("111111111111111");
            orderDao.deleteAll();
        } catch (Exception ignored) {
        }
    }
}
