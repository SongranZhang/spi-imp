package demo.spring;

public class COrderService implements OrderService {

    @Override
    public void getOrder(String msg) {
        System.out.println("c order -> " + msg);
    }
}
