package io.github.tranngockhoa.driver.proxy;

public class ProxyConfig {
    private String host;
    private String port;
    private String username;
    private String password;

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static ProxyConfigBuilder builder() {
        return new ProxyConfigBuilder();
    }

    public static final class ProxyConfigBuilder {
        private String host;
        private String port;
        private String username;
        private String password;

        private ProxyConfigBuilder() {
        }

        public ProxyConfigBuilder host(String host) {
            this.host = host;
            return this;
        }

        public ProxyConfigBuilder port(String port) {
            this.port = port;
            return this;
        }

        public ProxyConfigBuilder username(String username) {
            this.username = username;
            return this;
        }

        public ProxyConfigBuilder password(String password) {
            this.password = password;
            return this;
        }

        public ProxyConfig build() {
            ProxyConfig proxyConfig = new ProxyConfig();
            proxyConfig.host = host;
            proxyConfig.port = port;
            proxyConfig.username = username;
            proxyConfig.password = password;
            return proxyConfig;
        }
    }
}
