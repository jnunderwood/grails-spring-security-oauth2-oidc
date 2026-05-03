# Grails Spring Security OAuth2 OIDC Plugin

[![Java CI](https://github.com/grails-plugins/grails-spring-security-oauth2-oidc/actions/workflows/gradle.yml/badge.svg?branch=1.0.x)](https://github.com/grails-plugins/grails-spring-security-oauth2-oidc/actions/workflows/gradle.yml) 

Add an OIDC OAuth2 provider to the [Spring Security Plugin](https://github.com/apache/grails-spring-security).

## Installation

Add the following dependencies in `build.gradle`

```
dependencies {
...
    compile 'org.apache.grails:grails-spring-security-oauth2:7.0.1'
    compile 'org.grails.plugins:spring-security-oauth2-oidc:1.0.+'
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
                        domain: "https://your.oidc.provider.domain" # required (aka Issuer URI)
                        api_key: "oidc-api-key"                     # required (aka Client ID)
                        api_secret: "oidc-api-secret"               # required (aka Client Secret)
                        successUri: "/oauth2/oidc/success"          # optional (Default: "/oauth2/oidc/success")
                        failureUri: "/oauth2/oidc/failure"          # optional (Default: "/oauth2/oidc/failure")
                        callback: "/oauth2/oidc/callback"           # optional (Default: "/oauth2/oidc/callback")
                        scopes: "some_scope another_scope"          # optional (Default: "openid profile email")
                        scopeSeparator: " "                         # optional (Default: " ")
```

> Note that `providerNames` should not include `oidc` since the OAuth2 plugin will add all 
> providers defined under `oauth2.providers` to the list of provider names.

Some providers require the `domain` to include a tenant ID or other path segments, so the
plugin will use the full `domain` value as the Issuer URI when validating tokens. For example,
the `domain` might be `https://login.microsoftonline.com/{your_tenant_id}/v2.0` for Azure AD.

The plugin always downloads and parses the provider metadata JSON from the well-known OpenID
Connect configuration URL (`{domain}/.well-known/openid-configuration`), and uses that document
for `authorization_endpoint`, `token_endpoint`, and `userinfo_endpoint`.

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
