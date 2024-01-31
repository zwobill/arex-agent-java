package io.arex.inst.runtime.config;


import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AgentConfigLoader
 * <p>
 * Arex agent settings are stored in a map. The key is in the format of "arex.agent.<setting-name>" and is case-insensitive.
 * The map can be loaded from a file or from the remote server via Arex configuration service.
 * <p>
 * The configuration file can be one of the following locations. The later one will override the previous one.
 *     <li> The current working directory
 *     <li> ~/.arex/arex-agent.yml
 * <p>
 * The configuration file format is YAML. The following is an example:
 * <code>
 * <pre>
 *   arex:
 *     agent:
 *       app: MyApp
 *       enabled: true
 *       recording:
 *         file: /tmp/recording.txt
 *     server:
 *       host: http://localhost:8080
 * </pre>
 * </code>
 */
public interface AgentConfigLoader {
    /**
     * Load the settings. It can be loaded from a file or from the remote server via Arex configuration service.
     */
    Map<String, Object> load();
}
