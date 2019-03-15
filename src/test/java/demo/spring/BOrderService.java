package demo.spring;

import org.springframework.beans.factory.annotation.Autowired;

public class BOrderService implements OrderService {

    @Autowired
    private WareService wareService;

    @Override
    public void getOrder(String msg) {
        wareService.getWare("b order -> " + msg);
    }
}
