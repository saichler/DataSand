package org.datasand.filesystem;

import java.io.File;
import org.datasand.microservice.MicroServicesManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileSystemTest {

    private MicroServicesManager am1 = null;
    private MicroServicesManager am2 = null;
    private MicroServicesManager am3 = null;

    private FileManagerAgent fa1 = null;
    private FileManagerAgent fa2 = null;

    @Before
    public void before() {
        am1 = new MicroServicesManager(false);
        am2 = new MicroServicesManager(false);

        fa1 = new FileManagerAgent(am1);
        fa1.addRepository("repo", "./repo1");

        fa2 = new FileManagerAgent(am2);
        fa2.addRepository("repo", "./repo2");
    }

    @After
    public void after() {
        am1.shutdown();
        am2.shutdown();
        File f = new File("./repo1/tiny2.txt");
        f.delete();
        f = new File("./repo2/tiny.txt");
        f.delete();
    }

    @Test
    public void testFileSync() throws InterruptedException {
        while(fa1.getRepository("repo").getSize()!=fa2.getRepository("repo").getSize() || fa1.getRepository("repo").getSize()<2){
            Thread.sleep(1000);
        }
        Assert.assertEquals(2, fa1.getRepository("repo").getSize());
        Assert.assertEquals(2, fa2.getRepository("repo").getSize());
    }
}
