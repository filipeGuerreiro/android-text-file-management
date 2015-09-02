package pt.ulisboa.tecnico.cmov.airdesk.business;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by alex on 17-03-2015.
 */
public class FileLocalTest extends ApplicationTestCase<Application> {
    private Application app;
    private File testDir;
    private File tmpFile;
    private FileLocal fl;

    public FileLocalTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.createApplication();

        app = this.getApplication();
        tmpFile = new File(getContext().getCacheDir(),"test.txt");
        fl = new FileLocal(tmpFile);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWrite() {

        char[] outPut = new char[254];

        assertTrue("FileLocal.write return false", fl.write("123"));

        try {
            BufferedReader br = new BufferedReader(new FileReader(tmpFile), 254);
            assertEquals(3, br.read(outPut));
            assertTrue("123".equals(String.valueOf(outPut).trim()));

        }
        catch (Exception e) {
            this.fail(e.toString());
        }

    }

    public void testRead() {
        assertTrue(FileUtil.writeStringAsFile("123", this.tmpFile));

        assertEquals("123", fl.read().trim());
    }

    public void testDel() {
        try {
            //Arrange
            tmpFile.createNewFile();
            assertTrue(tmpFile.exists());

            //Act
            fl.del();

            //Assert
            assertFalse(tmpFile.exists());

        } catch (IOException e) {
            fail("Unexpected Exception");
        }
    }
}
