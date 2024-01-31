package io.arex.foundation.services;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.service.DataCollector;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This is an implementation of DataCollector interface that writes all AREX collected test case data into a text file.
 * The file is located at ~/.arex/recording/<app-name>/<current-date>/recording.txt by default. If you want to change
 * the default location, you can set the value "arex.app.recording.file" to the new location in the configuration file.
 */
public class LocalFileDataCollector implements DataCollector {
    private static final String DEFAULT_FILE_NAME = "recording.txt";
    private static final String DEFAULT_FILE_PATH = System.getProperty("user.home") + "/.arex/recording";
    private static final String DEFAULT_FILE = DEFAULT_FILE_PATH + "/" + DEFAULT_FILE_NAME;
    private BufferedOutputStream outputStream = null;


    @Override
    public void start() {
        try {
            // Open the output file for writing
            outputStream = new BufferedOutputStream(
                    Files.newOutputStream(Paths.get(DEFAULT_FILE))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Mocker entry) {
        if (outputStream == null) {
            return; // fail silently?
        }

        try {
            // Write the test case data into the output file
            outputStream.write(entry.toString().getBytes());
            outputStream.write("\n".getBytes());
        } catch (IOException e) {
            return; // fail silently?
        }
    }

    @Override
    public void invalidCase(String postData) {
        if (outputStream == null) {
            return; // fail silently?
        }

        try {
            // Write the test case data into the output file
            outputStream.write(postData.getBytes());
            outputStream.write("\n".getBytes());
        } catch (IOException e) {
            return; // fail silently?
        }
    }

    @Override
    public String query(String postData, MockStrategyEnum mockStrategy) {
        return null;
    }
}
