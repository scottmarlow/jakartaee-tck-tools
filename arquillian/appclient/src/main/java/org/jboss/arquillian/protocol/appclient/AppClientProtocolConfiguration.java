package org.jboss.arquillian.protocol.appclient;

import org.jboss.arquillian.container.test.spi.client.protocol.ProtocolConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AppClientProtocolConfiguration implements ProtocolConfiguration {
    private boolean runClient;
    /**
     * Provide an optional envp array to pass as is to {@link Runtime#exec(String[], String[])}
     * @return a possibly empty string providing env1=value1;env2=value2 environment variable settings
     */
    private String clientEnvString;
    /**
     * A comma separated string for the command line arguments to pass as the cmdarray to {@link Runtime#exec(String[], String[])}
     */
    private String clientCmdLineString;
    /**
     * An optional directory string to use as the appclient working directory. This is passed as the dir arguemnt
     * to {@link Runtime#exec(String[], String[], File)}
     */
    private String clientDir;

    public boolean isRunClient() {
        return runClient;
    }

    public void setRunClient(boolean runClient) {
        this.runClient = runClient;
    }

    public String getClientEnvString() {
        return clientEnvString;
    }

    public void setClientEnvString(String clientEnvString) {
        this.clientEnvString = clientEnvString;
    }

    public String getClientCmdLineString() {
        return clientCmdLineString;
    }

    public void setClientCmdLineString(String clientCmdLineString) {
        this.clientCmdLineString = clientCmdLineString;
    }

    public String getClientDir() {
        return clientDir;
    }
    public void setClientDir(String clientDir) {
        this.clientDir = clientDir;
    }

    // Helper methods to turn the strings into the types used by Runtime#exec
    public File clientDirAsFile() {
        File dir = null;
        if (clientDir != null) {
            dir = new File(clientDir);
        }
        return dir;
    }

    public String[] clientCmdLineAsArray() {
        return clientCmdLineString.trim().split(";");
    }
    public String[] clientEnvAsArray() {
        String[] envp = null;
        if (clientEnvString != null) {
            ArrayList<String> tmp = new ArrayList<String>();
            // First split on the env1=value1 ; separator
            String[] pairs = clientEnvString.trim().split(";");
            // Now parse env1=value1 by breaking on the first '='
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                String env = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                tmp.add(env);
                tmp.add(value);
            }
            envp = tmp.toArray(new String[tmp.size()]);
        }
        return envp;
    }
}
