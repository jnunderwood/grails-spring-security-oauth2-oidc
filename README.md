# Grails Spring Security OAuth2 OIDC Plugin

[![Java CI](https://github.com/grails-plugins/grails-spring-security-oauth2-oidc/actions/workflows/gradle.yml/badge.svg?branch=1.0.x)](https://github.com/grails-plugins/grails-spring-security-oauth2-oidc/actions/workflows/gradle.yml) 

Add an OIDC OAuth2 provider to the [Grails Core - Spring Security Plugin](https://github.com/apache/grails-core)
([docs](https://grails.apache.org/docs/7.2.2/guide/security.html#springSecurityOauth2)).

## Installation

Add the following dependencies in `build.gradle`

```
dependencies {
...
    implementation 'org.apache.grails:grails-spring-security-oauth2:7.2.2'
    implementation 'org.grails.plugins:spring-security-oauth2-oidc:1.0.+'
...
}
```

## Configuration

Add this to your `application.yml`:

```yaml
grails:
    plugin:
        springsecurity:
            auth:
                loginFormUrl: '/oauth2/oidc/authenticate'
            providerNames: [ 'additional_provider', 'additional_provider', 'etc.' ]
            oauth2:
                providers:
                    oidc:
                        # these settings are required
                        domain: "https://your.oidc.provider.domain" # aka Issuer URI
                        api_key: "oidc-api-key"                     # aka Client ID
                        api_secret: "oidc-api-secret"               # aka Client Secret
                        
                        # these settings are optional and will default to the values shown below if not provided
                        successUri: "/oauth2/oidc/success"          # Default: "/oauth2/oidc/success"
                        failureUri: "/oauth2/oidc/failure"          # Default: "/oauth2/oidc/failure"
                        callback: "/oauth2/oidc/callback"           # Default: "/oauth2/oidc/callback"
                        scopes: "some_scope another_scope"          # Default: "openid profile email"
                        scopeSeparator: " "                         # Default: " "
                        
                        # these endpoints are optional and will be discovered from OIDC config URL if not provided
                        authorizationEndpoint: "https://your.oidc.provider.domain/oauth2/authorize"
                        tokenEndpoint: "https://your.oidc.provider.domain/oauth2/token"
                        userInfoEndpoint: "https://your.oidc.provider.domain/openid/userinfo"
                        issuer: "https://your.oidc.provider.domain/"
                        logoutUri: "https://your.oidc.provider.domain/oauth2/logout"
                        jwksUri: "https://your.oidc.provider.domain/oauth2/keys"
```

> Note that `springsecurity.providerNames` should not include `oidc` since the OAuth2 plugin 
> will add all providers defined under `oauth2.providers` to the list of provider names. Of
> course, you may not even need that setting at all depending on your requirements.

Some providers require the `domain` to include a tenant ID or other path segments, so the
plugin will use the full `domain` value as the Issuer URI when validating tokens. For example,
the `domain` might be `https://login.microsoftonline.com/{your_tenant_id}/v2.0` for recent
releases of Azure AD.

The plugin will try to obtain the endpoint URIs from the user configuration first, and if any 
of the endpoints are not provided, it will attempt to discover them from the OpenID Connect
configuration URL (`{domain}/.well-known/openid-configuration`). 

You can replace the `successUri`, `failureUri`, and `callback` default values with your own 
controller implementation.

## Usage

In your views you can use the taglib exposed from this plugin and the OAuth plugin
to create links and to know if the user is authenticated with a given provider:

```html
<oauth2:connect provider="oidc" id="oidc-connect-link">OIDC Provider</oauth2:connect>

Logged in with OIDC?
<oauth2:ifLoggedInWith provider="oidc">yes</oauth2:ifLoggedInWith>
<oauth2:ifNotLoggedInWith provider="oidc">no</oauth2:ifNotLoggedInWith>
```

## Versions

| Branch | Grails Version |
|--------|----------------|
| 1.0.x  | 7.x            |

## License

Apache 2
