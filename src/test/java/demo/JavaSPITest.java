package demo;

import demo.java.Robot;
import org.junit.Test;

import java.util.ServiceLoader;

public class JavaSPITest {

    @Test
    public void sayHello() throws Exception {
        ServiceLoader<Robot> serviceLoader = ServiceLoader.load(Robot.class);
        System.out.println("Java SPI");
        for (Robot robot : serviceLoader) {
            robot.sayHello();
        }
    }
}
