package maven.springcloud.quickstart.test;

import maven.springcloud.quickstart.Application;
import maven.springcloud.quickstart.api.AFeign;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The unit test
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        01/01/2021        Initialize  *
 * *****************************************************************
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Rollback(false)
public class UnitTest {
    @Autowired
    private AFeign aFeign;

    @Test
    public void test00_invoke() throws InterruptedException {
        Thread.sleep(10000);
        System.out.println(this.aFeign.invoke("同步调用"));
    }

    @Test
    public void test01_invokeAsync() {
        System.out.println(this.aFeign.invokeThread("线程调用"));
    }
}