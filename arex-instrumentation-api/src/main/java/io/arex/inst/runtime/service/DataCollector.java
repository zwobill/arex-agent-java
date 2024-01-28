package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;

/**
 * Data collector interface. It defines the basic operations for AREX data storage which is used to 
 * store and retrieve AREX data entries.
 */
public interface DataCollector {
    /**
     * Initialize data collector.
     */
    void start();

    /**
     * Save a mock entry. Depends on the type of Arex storage backend is been used, the data may
     * be saved into local file storage or a remote Arex server.
     * @param requestMocker the Arex data entry. It can be either request and/or response with additional
     * transactional context data such as trace id, API name, etc. 
     */
    void save(Mocker entry);

    /**
     * Save a mock entry. Depends on the type of Arex storage backend is been used, the data may
     * be saved into local file storage or a remote Arex server.
     * @param requestMocker the Arex data entry. It can be either request and/or response with additional
     * transactional context data such as trace id, API name, etc. 
     */
    void invalidCase(String postData);

    /**
     * Query a mock entry. Depends on the type of Arex storage backend is been used, the data may be queried
     * from local file storage or a remote Arex server.
     * @param postData
     * @param mockStrategy
     * @return the mock response data.
     */
    String query(String postData, MockStrategyEnum mockStrategy);
}
