package demo;


import demo.spring.OrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring.xml"})
public class SpringSPITest {

    @Test
    public void sayHello() throws Exception {
        List<OrderService> services = SpringFactoriesLoader.loadFactories(OrderService.class, null);
        for (OrderService service : services) {
            service.getOrder("worder.");
        }
    }
}
