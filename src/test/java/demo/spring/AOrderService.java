package demo.spring;

import org.springframework.beans.factory.annotation.Autowired;

public class AOrderService implements OrderService {

    @Autowired
    private WareService wareService;

    @Override
    public void getOrder(String msg) {
        wareService.getWare("a order -> " + msg);
    }
}
