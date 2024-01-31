package io.arex.inst.runtime.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AgentConfigTest {
    private AgentConfig config = null;

    // Load a config map from a YAML file in test resources
    private static Map<String, Object> loadTestYamlConfig() throws IOException {
        return new Yaml().load(AgentConfigTest.class.getResourceAsStream("/test-config.yml"));
    }

    private Map<String, Object> configMap ;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        try {
            // Load the config map from the test resource file
            configMap = loadTestYamlConfig();
            String methodName = testInfo.getTestMethod().orElseThrow().getName();
            if (!methodName.equals("load")) {
                config = new AgentConfig().load(configMap);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        config = null;
    }

    @Test
    void load() {
        AgentConfig config = new AgentConfig().load(configMap);
        assertEquals("test-app", config.getAppName());
    }

    @Test
    void get() {
        assertEquals("test-app", config.get("arex.app.name"));
    }

    @Test
    void getInvalidKey() {
        assertThrows(IllegalArgumentException.class, () -> config.get("invalid-key"));
    }

    @Test
    void isAgentEnabled() {
        assertTrue(config.isAgentEnabled());
    }

    @Test
    void isDebugEnabled() {
        assertTrue(config.isDebugEnabled());
    }

    @Test
    void getAppName() {
        assertEquals("test-app", config.getAppName());
    }

    @Test
    void getDynamicClassList() {

    }

    @Test
    void getDynamicClassSignatureMap() {
    }

    @Test
    void getDynamicAbstractClassList() {
    }

    @Test
    void getExcludeServiceOperations() {
        var excludeServiceOperations = config.getExcludeServiceOperations();
        assertTrue(excludeServiceOperations.contains("excludedApi1"));
        assertTrue(excludeServiceOperations.contains("excludedApi2"));
    }

    @Test
    void getRecordRate() {
        assertEquals(100, config.getRecordRate());
    }

    @Test
    void getIncludeServiceOperations() {
        var includeServiceOperations = config.getIncludeServiceOperations();
        assertTrue(includeServiceOperations.contains("includedApi1"));
        assertTrue(includeServiceOperations.contains("includedApi2"));
    }
}