package io.arex.inst.runtime.config;

import com.google.common.annotations.VisibleForTesting;
import io.arex.inst.runtime.model.DynamicClassEntity;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is the main source of truth for all the agent settings. The settings are stored in a map. The configure values
 * can be looked up by key, which is in the format of "arex.agent.<setting-name>" and is case-sensitive.
 * <p>The AgentConfig is designed to be very lightweight and it is read only. The heavy lifting code that loads values
 * from file system or remote server will be done via SPI implementations of interface<a>AgentConfigLoader</a>.
 * These classes will be loaded in <a>AgentClassLoader</a> accessed via the ServiceLoader mechanism.</p>
 * <p>There should be only one single instance of AgentConfig that is globally available to all components of Arex.
 * It is a singleton class and the singleton instance can be accessed via <a>getInstance</a> method.</p>
 *
 * <code>
 * <pre>
 *   AgentConfig config = AgentConfig.getInstance();
 *   String appName = (String) config.get("arex.agent.app.name");
 * </pre>
 * </code>
 */
public class AgentConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentConfig.class);

    // The key is in the format of "arex.agent.<setting-name>" and is case-sensitive.
    static final Pattern KEY_PATTERN = Pattern.compile("^arex\\.[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*$");
    private Map<String, Object> configMap; // the config map loaded from the config file or from the remote server

    // The following fields are the settings that are frequently used. They are cached for performance.
    private boolean agentEnabled;
    private boolean debugEnabled;
    private String appName;
    private List<DynamicClassEntity> dynamicClassList;
    private Map<String, DynamicClassEntity> dynamicClassSignatureMap;
    private String[] dynamicAbstractClassList;
    private Set<String> excludeServiceOperations;
    private int recordRate;
    private Set<String> includeServiceOperations;

    /**
     * Singleton instance
     */
    private static final AgentConfig instance = new AgentConfig();

    /**
     * Get the singleton instance
     * @return the singleton instance
     */
    public static AgentConfig getInstance() {
        return instance;
    }

    AgentConfig() {
    }

    /**
     * Load config from the config Map object supplied. If the object is <code>null</code>, this method will try to
     * load from config file or from the remote server via AgentConfig SPI implementations.
     */
    public AgentConfig load(Map<String, Object> m) {
        try {
            configMap = (m == null) ? getAgentConfigLoader().load() : m;
            updateCachedFields();
        } catch (Exception e) {
            // TODO: What to do if the config is not loaded or misconfigured?
            LOGGER.error("Failed to load agent configuration.", e);
        }

        // TODO: validate the configuration

        return this;
    }

    /**
     * Get the setting value by key. The key is in the format of "arex.agent.<setting-name>" and is case-sensitive.
     * @param key the setting key
     *            <p>
     *            <b>arex.agent.app.name</b> - the application name
     *            <p>
     *            <b>arex.agent.app.recording.file</b> - the file path to store the recording data
     *            <p>
     * @return the setting value
     */
    public Object get(String key) {
        validateKey(key);
        String[] parts = key.split("\\.");
        if (configMap == null) {
            throw new IllegalStateException("AgentConfig is not loaded yet. Please call load() first.");
        }
        return recursiveGet(configMap, parts, 0, key);
    }

    /**
     * Helper function that gets the setting value from a map by looking up the key in the map.
     *
     * @param currentMap the map to look up the key
     * @param parts the key parts
     * @param index the index of the current part
     * @param key the whole key string for error message
     * @return the setting value
     */
    @SuppressWarnings("unchecked")
    private Object recursiveGet(Map<String, Object> currentMap, String[] parts, int index, String key) {
        if (index == parts.length - 1) {
            return currentMap.get(parts[index]);
        }
        // Find the value of the current part and do it recursively
        Object value = currentMap.get(parts[index]);
        if (value instanceof Map) {
            return recursiveGet((Map<String, Object>) value, parts, index + 1, key);
        } else {
            throw new IllegalArgumentException("Invalid setting key: " + key);
        }
    }

    /**
     * Helper function that validates the key format by REGEX matching.
     */
    private void validateKey(String key) {
        Matcher matcher = KEY_PATTERN.matcher(key);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid setting key: " + key);
        }
    }

    /**
     * Find the SPI implementation classes from the SPI implementation classes.
     * @return the AgentConfigLoader instance
     */
    private AgentConfigLoader getAgentConfigLoader() {
        // Find the SPI implementation classes and load the configuration from the SPI implementation classes.
        ServiceLoader<AgentConfigLoader> services = ServiceLoader.load(AgentConfigLoader.class);

        for (AgentConfigLoader service : services) {
            return service;
        }

        throw new RuntimeException("No AgentConfigLoader implementation found");
    }

    /**
     * Set the cached settings fields when a new config map is loaded.
     */
    @SuppressWarnings("unchecked")
    private void updateCachedFields() {
        // TODO: object deserialization could be better?
        agentEnabled = (boolean) get("arex.agent.enabled");
        debugEnabled = (boolean) get("arex.agent.debug");
        recordRate = (int) get("arex.agent.recording.rate");
        appName = (String) get("arex.app.name");
        excludeServiceOperations = new HashSet<>((List<String>) get("arex.app.excludedApi"));
        includeServiceOperations = new HashSet<>((List<String>) get("arex.app.includedApi"));
        dynamicAbstractClassList = (String[]) get("arex.app.dynamicAbstractClasses");
//        dynamicClassList = ((List<Map<String, Object>>) get("arex.agent.dynamicClasses")).stream()
//                .map(DynamicClassEntity::fromMap).collect(Collectors.toList());
//        dynamicClassSignatureMap = ((List<Map<String, Object>>) get("arex.agent.dynamicClassSignatures")).stream()
//                .map(DynamicClassEntity::fromMap).collect(Collectors.toMap(DynamicClassEntity::getSignature, c -> c));
    }

    public boolean isAgentEnabled() {
        return agentEnabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public String getAppName() {
        return appName;
    }

    public List<DynamicClassEntity> getDynamicClassList() {
        return dynamicClassList;
    }

    public Map<String, DynamicClassEntity> getDynamicClassSignatureMap() {
        return dynamicClassSignatureMap;
    }

    public String[] getDynamicAbstractClassList() {
        return dynamicAbstractClassList;
    }

    public Set<String> getExcludeServiceOperations() {
        return excludeServiceOperations;
    }

    public int getRecordRate() {
        return recordRate;
    }

    public Set<String> getIncludeServiceOperations() {
        return includeServiceOperations;
    }
}
