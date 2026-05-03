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

import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor
import com.github.scribejava.core.extractors.TokenExtractor
import com.github.scribejava.core.model.OAuth2AccessToken

class OidcOauth2Api extends DefaultApi20 {

    final static String PROVIDER_ID = 'oidc'

    final OidcOauth2Config oidcOauth2Config

    OidcOauth2Api(OidcOauth2Config oidcOauth2Config) {
        this.oidcOauth2Config = oidcOauth2Config
    }

    @Override
    String getAccessTokenEndpoint() {
        return oidcOauth2Config.tokenEndpoint
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return oidcOauth2Config.authorizationEndpoint
    }

    @Override
    TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        // TODO: this code is the same as default extractor in the superclass; can we remove this method?
        return OAuth2AccessTokenJsonExtractor.instance()
    }
}
