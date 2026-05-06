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
import groovy.util.logging.Slf4j

/**
 * Configuration class for OIDC OAuth2 provider settings. This class extends the base
 * OAuth2ProviderConfiguration and adds OIDC-specific properties and logic to lazily
 * load the provider's metadata from the OIDC discovery document. The configuration
 * values can be set by the user or automatically loaded from the provider's metadata
 * when accessed.
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = 'grails.plugin.springsecurity.oauth2.providers.oidc')
class OidcOauth2Config extends OAuth2ProviderConfiguration {

    // these values are user-configured
    String domain // the issuer URL of the OIDC provider; required
    String scope // configured by the 'scopes' (plural) property; optional
    String scopeSeparator // optional

    // these values are user-configured OR lazily loaded from the OIDC discovery document
    String authorizationEndpoint
    String tokenEndpoint
    String userInfoEndpoint
    String issuer
    String logoutUri
    String jwksUri

    private volatile boolean openIdConfigurationLoaded
    private Map<String, Object> metadata

    String getDomain() {
        if (!this.@domain) {
            throw new IllegalStateException("Missing required OIDC 'domain'")
        }
        this.@domain
    }

    String getAuthorizationEndpoint() {
        if (!this.@authorizationEndpoint) {
            initializeOpenIdConfiguration()
            this.@authorizationEndpoint = readUrlFromMetadata('authorization_endpoint')
        }
        this.@authorizationEndpoint
    }

    String getTokenEndpoint() {
        if (!this.@tokenEndpoint) {
            initializeOpenIdConfiguration()
            this.@tokenEndpoint = readUrlFromMetadata('token_endpoint')
        }
        this.@tokenEndpoint
    }

    String getUserInfoEndpoint() {
        if (!this.@userInfoEndpoint) {
            initializeOpenIdConfiguration()
            this.@userInfoEndpoint = readUrlFromMetadata('userinfo_endpoint')
        }
        this.@userInfoEndpoint
    }

    String getIssuer() {
        if (!this.@issuer) {
            initializeOpenIdConfiguration()
            this.@issuer = readUrlFromMetadata('issuer')
        }
        this.@issuer
    }

    // TODO: currently unused, but may be useful in the future
    String getLogoutUri() {
        if (!this.@logoutUri) {
            initializeOpenIdConfiguration()
            this.@logoutUri = readUrlFromMetadata('end_session_endpoint', false)
        }
        this.@logoutUri
    }

    URL getJwksUrl() {
        if (!this.@jwksUri) {
            initializeOpenIdConfiguration()
            this.@jwksUri = readUrlFromMetadata('jwks_uri')
        }

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
     * or parsing fails, or if the resulting JSON is not a Map.
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
     * Reads a URL value from OIDC metadata and validates that it is absolute.
     *
     * If {@code required} is true, missing/empty values raise an exception.
     * If {@code required} is false, missing/empty values return null.
     */
    private String readUrlFromMetadata(String key, boolean required = true) {
        log.debug("Reading OIDC metadata key '$key'")
        def rawValue = metadata[key]
        String value = rawValue instanceof CharSequence ? rawValue.toString() : null
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
     * Lazily loads the OIDC provider metadata from the discovery document if
     * it has not already been loaded. This method is thread-safe and ensures
     * that the metadata is only loaded once, even if multiple threads call it
     * concurrently.
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
            log.debug("Loading OIDC configuration from provider metadata at ${configurationUrl}")
            metadata = downloadJson(configurationUrl)
            openIdConfigurationLoaded = true
        }
    }
}
