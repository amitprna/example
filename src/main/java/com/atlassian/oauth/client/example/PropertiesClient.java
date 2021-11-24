package com.atlassian.oauth.client.example;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertiesClient {
    public static final String CONSUMER_KEY = "consumer_key";
    public static final String PRIVATE_KEY = "private_key";
    public static final String REQUEST_TOKEN = "request_token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String SECRET = "secret";
    public static final String JIRA_HOME = "jira_home";


    private final static Map<String, String> DEFAULT_PROPERTY_VALUES = ImmutableMap.<String, String>builder()
            .put(JIRA_HOME, "http://localhost:8090/jira")
            .put(CONSUMER_KEY, "OauthKey")
            .put(PRIVATE_KEY, "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKieR2CHjEZ5agN2OPXE/LNAkBkX3BSIvROfmOzXxIHyePZMkFibWhSQdwPHNVSo25CrH/R1RRRREimBk594oz+vsRz6nywbucxBH3lhoH4VRWWcBrQGHJ+NOHHLjAO2/4dg4hIAsZVS51kz0m/U/Q4A5Cb1hcPAgMBAAECgYA+90QeNt/aHVLKUiZ5GINpCK4GwNMtU9aPcmSv5O77u6kSCItKm1CrJztaTNSMdbXwMwbuya/vI0sN9tZCkdEbxqjwK+XpeUdkDnTQuyp+EvMyPsthFnLjj0WZYTPSkaFpSefO4btenCN6tRsGeVxds5+B2gDpnifGeHnlNZTlYQJBAN2ULQqr+uJm2gP1RWAWHV9i7uL0Ika5bfJ2i4On8L/SJ+jlPd2z0eiLtfXEma/9ZxTvShMezUrBny93JAkuvd8CQQDCz/Med4uRv4MpD6RXc0n295IujvTaLVUgcOhP7tRy5NSEuj1CVrn0L0a5OrS9dfSOF56WhFK4pLjZzqHHzWzRAkAPYig2Y3ZJnaZpO/ATGbX0V2QKcbSngFw3xy8sds5qIucObkv7Rm6+XGTHqVyaMVekxnA4H51VIVgx7W2dDZe1AkA2vttyd2PIbIo+Ur/7N1HUzlPNM61JVq3ydOhD9jjLpNS4C+JFLzwIi/SS/xVlnK1B9Q9C+yKH8vpjcvbxwB/RAkEAwxRDUltODxBrIu1zfuSr/TyiHWatyWZ+GifoPyHrlZhfhE+PjmxrmtQOwF6QL940dA8lI5lyJdjN8ic1pGVufQ==")
            .build();

    private final String fileUrl;
    private final String propFileName = "config.properties";

    public PropertiesClient() throws Exception {
        fileUrl = "./" + propFileName;
    }

    public Map<String, String> getPropertiesOrDefaults() {
        try {
            Map<String, String> map = toMap(tryGetProperties());
            map.putAll(Maps.difference(map, DEFAULT_PROPERTY_VALUES).entriesOnlyOnRight());
            return map;
        } catch (FileNotFoundException e) {
            tryCreateDefaultFile();
            return new HashMap<>(DEFAULT_PROPERTY_VALUES);
        } catch (IOException e) {
            return new HashMap<>(DEFAULT_PROPERTY_VALUES);
        }
    }

    private Map<String, String> toMap(Properties properties) {
        return properties.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(o -> o.getKey().toString(), t -> t.getValue().toString()));
    }

    private Properties toProperties(Map<String, String> propertiesMap) {
        Properties properties = new Properties();
        propertiesMap.entrySet()
                .stream()
                .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
        return properties;
    }

    private Properties tryGetProperties() throws IOException {
        InputStream inputStream = new FileInputStream(new File(fileUrl));
        Properties prop = new Properties();
        prop.load(inputStream);
        return prop;
    }

    public void savePropertiesToFile(Map<String, String> properties) {
        OutputStream outputStream = null;
        File file = new File(fileUrl);

        try {
            outputStream = new FileOutputStream(file);
            Properties p = toProperties(properties);
            p.store(outputStream, null);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            closeQuietly(outputStream);
        }
    }

    public void tryCreateDefaultFile() {
        System.out.println("Creating default properties file: " + propFileName);
        tryCreateFile().ifPresent(file -> savePropertiesToFile(DEFAULT_PROPERTY_VALUES));
    }

    private Optional<File> tryCreateFile() {
        try {
            File file = new File(fileUrl);
            file.createNewFile();
            return Optional.of(file);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // ignored
        }
    }
}
