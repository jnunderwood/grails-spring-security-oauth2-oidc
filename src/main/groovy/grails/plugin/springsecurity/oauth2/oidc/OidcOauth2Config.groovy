/* Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugin.springsecurity.oauth2.oidc

import grails.plugin.springsecurity.oauth2.util.OAuth2ProviderConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import groovy.json.JsonSlurper

/**
 * Configuration class that holds both the user-configured OIDC provider settings and the lazily loaded
 * OIDC metadata values from the provider's discovery document. The OIDC metadata values are loaded
 * on demand when one of the corresponding getters is called for the first time, and are cached for
 * subsequent calls.
 */
@Configuration
@ConfigurationProperties(prefix = 'grails.plugin.springsecurity.oauth2.providers.oidc')
class OidcOauth2Config extends OAuth2ProviderConfiguration {

    // these values are configured by the user
    String domain // the issuer URL of the OIDC provider; required
    String scope // configured by the 'scopes' (plural) property; optional
    String scopeSeparator // optional

    // these values are lazily loaded from the OIDC discovery document
    String authorizationEndpoint
    String tokenEndpoint
    String userInfoEndpoint
    String issuer
    String logoutUri
    String jwksUri

    // has the OIDC configuration has been loaded? used to ensure thread-safe initialization
    private volatile boolean openIdConfigurationLoaded

    String getDomain() {
        if (!this.@domain) {
            throw new IllegalStateException("Missing required OIDC 'domain'")
        }
        this.@domain
    }

    String getAuthorizationEndpoint() {
        initializeOpenIdConfiguration()
        this.@authorizationEndpoint
    }

    String getTokenEndpoint() {
        initializeOpenIdConfiguration()
        this.@tokenEndpoint
    }

    String getUserInfoEndpoint() {
        initializeOpenIdConfiguration()
        this.@userInfoEndpoint
    }

    String getIssuer() {
        initializeOpenIdConfiguration()
        this.@issuer
    }

    // TODO: currently unused, but may be useful in the future
    String getLogoutUri() {
        initializeOpenIdConfiguration()
        this.@logoutUri
    }

    URL getJwksUrl() {
        initializeOpenIdConfiguration()
        try {
            return URI.create(this.@jwksUri).normalize().toURL()
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWKS URI '${this.@jwksUri}': ${e.message}", e)
        }
    }

    String getDomainWithTrailingSlash() {
        domain.endsWith('/') ? domain : domain + '/'
    }

    String getDomainWithoutTrailingSlash() {
        domain.endsWith('/') ? domain[0..-2] : domain
    }

    /**
     * Downloads and parses the OIDC discovery document from the given URL,
     * returning the metadata as a map. Throws an exception if the download
     * or parsing fails, or if the resulting JSON is not an object.
     */
    private static Map<String, Object> downloadJson(String jsonUrl) {
        try {
            URL url = URI.create(jsonUrl).normalize().toURL()
            def data = new JsonSlurper().parse(
                    url,
                    connectTimeout: 10_000,
                    readTimeout: 10_000,
                    requestProperties: ['Accept': 'application/json']
            )

            if (data !instanceof Map) {
                throw new IllegalStateException("Expected a JSON object from '$jsonUrl'")
            }

            return data as Map<String, Object>
        } catch (IllegalStateException e) {
            throw e
        } catch (Exception e) {
            throw new IllegalStateException("Failed to download or parse OIDC metadata from '$jsonUrl': ${e.message}", e)
        }
    }

    /**
     * Reads a required URL value from the OIDC metadata map and validates that
     * it is an absolute URL. If the key is missing or empty, either returns the
     * provided default value (if not null) or throws an exception.
     */
    private static String readUrlFromMetadata(Map<String, Object> metadata, String key, boolean required = true) {
        def rawValue = metadata[key]
        String value = rawValue instanceof CharSequence ? rawValue.toString() : ''
        if (!value) {
            if (!required) {
                return value
            }
            throw new IllegalStateException("Missing required OIDC metadata key '$key' or value is empty")
        }

        if (!isAbsoluteUrl(value)) {
            throw new IllegalStateException("OIDC metadata value for '$key' must be an absolute URL")
        }
        value
    }

    /**
     * Validates that the given string is an absolute URL.
     */
    private static boolean isAbsoluteUrl(String url) {
        try {
            URI uri = URI.create(url)
            return uri.absolute
        } catch (Exception ignored) {
            return false
        }
    }

    /**
     * Applies the OIDC configuration values from the given metadata map to the backing fields.
     */
    private void applyOpenIdConfiguration(Map<String, Object> metadata) {
        // Use backing fields here to avoid triggering getters during lazy initialization
        this.@authorizationEndpoint = readUrlFromMetadata(metadata, 'authorization_endpoint')
        this.@tokenEndpoint = readUrlFromMetadata(metadata, 'token_endpoint')
        this.@userInfoEndpoint = readUrlFromMetadata(metadata, 'userinfo_endpoint', false)
        this.@issuer = readUrlFromMetadata(metadata, 'issuer', false)
        this.@logoutUri = readUrlFromMetadata(metadata, 'end_session_endpoint', false)
        this.@jwksUri = readUrlFromMetadata(metadata, 'jwks_uri', false)
    }

    /**
     * Initializes the OIDC configuration by downloading the provider's discovery document and
     * applying the configuration values. This method is thread-safe and will only perform the
     * initialization once, even if called concurrently.
     */
    private void initializeOpenIdConfiguration() {
        if (openIdConfigurationLoaded) {
            return
        }

        synchronized (this) {
            if (openIdConfigurationLoaded) {
                return
            }

            String configurationUrl = domainWithoutTrailingSlash + '/.well-known/openid-configuration'
            def metadata = downloadJson(configurationUrl)
            applyOpenIdConfiguration(metadata)
            openIdConfigurationLoaded = true
        }
    }
}
