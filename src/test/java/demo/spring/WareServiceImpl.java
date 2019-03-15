package demo.spring;

public class WareServiceImpl implements WareService {

    @Override
    public void getWare(String msg) {
        System.out.println("getWare print: " + msg);
    }
}
