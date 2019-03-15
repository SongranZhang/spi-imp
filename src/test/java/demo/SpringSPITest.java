package demo;

import com.linkedkeeper.spi.ExtensionLoader;
import demo.spring.OrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring.xml"})
public class SpringSPITest {

    @Autowired
    private ExtensionLoader loader;

    @Test
    public void sayHello() throws Exception {
        OrderService orderService = (OrderService) loader.getExtensionLoader(OrderService.class).get("japan");
        orderService.getOrder("hello.");
    }
}
