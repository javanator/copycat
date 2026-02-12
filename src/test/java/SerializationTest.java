import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkitmodders.copycat.model.PluginConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SerializationTest
{
    private ObjectMapper objectMapper=new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT);


    @Before
    public void beforeClass(){
    }

    @Test
    public void pluginConfigTest() throws IOException {
        PluginConfig pluginConfig = objectMapper.readValue(this.getClass().getResourceAsStream("defaultSettings.json"), PluginConfig.class);

        return;
    }

    /**
     * Used to test the PaperMC configuration launched via gradle.
     * @throws IOException
     */
    @Test
    @Ignore
    public void serverConfigTest() throws IOException {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        File config = new File("./run/plugins/Copycat/pluginSettings.json");
        FileInputStream inputStream = new FileInputStream(config);

        PluginConfig pluginConfig = objectMapper.readValue(inputStream, PluginConfig.class);
    }
}
