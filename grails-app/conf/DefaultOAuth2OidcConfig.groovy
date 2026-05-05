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

security {
    oauth2 {
        providers {
            oidc {
                domain = "https://your.oidc.provider.domain"
                api_key = "changeme_apikey"
                api_secret = "changeme_apisecret"
                successUri = "/oauth2/oidc/success"
                failureUri = "/oauth2/oidc/failure"
                callback = "/oauth2/oidc/callback"
                scopes = "openid profile email"
                scopeSeparator = " "
                authorizationEndpoint = "https://your.oidc.provider.domain/oauth2/authorize"
                tokenEndpoint = "https://your.oidc.provider.domain/oauth2/token"
                userInfoEndpoint = "https://your.oidc.provider.domain/openid/userinfo"
                issuer = "https://your.oidc.provider.domain/"
                logoutUri = "https://your.oidc.provider.domain/oauth2/logout"
                jwksUri = "https://your.oidc.provider.domain/oauth2/keys"
            }
        }
    }
}
