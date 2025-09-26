import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bukkitmodders.copycat.model.PluginConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SerializationTest
{
    private ObjectMapper objectMapper=new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);


    @Before
    public void beforeClass(){
    }

    @Test
    public void pluginConfigTest() throws IOException {
        PluginConfig pluginConfig = objectMapper.readValue(this.getClass().getResourceAsStream("defaultSettings.json"), PluginConfig.class);

        return;
    }

}
