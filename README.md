# oauth2-server

## Why?
* Because nobody can agree on what OAuth2 is.
* Because I'm sick and tired of starting a new project and having to implement an auth layer again.

## Design decisions
* This server is for authorization, and LIMITED authentication. We really don't want to store passwords.
* Jersey2 as the JAX-RS implementation, because it's what I'm familiar with.
* Injection framework: HK2 (since it comes with Jersey2)
* Jackson to deserialize things.
* All this server does is handle user identity and claims returned from the authentication host.

## Things that aren't in the spec.
* How do we register users that aren't implicitly provided by something like google?
* How would a third party service validate that a token it was handed is valid?
* Should we bother layering on something like OpenID Connect which can return JWT user data claims?

## Example protocol flow: Google Auth
* Browser App redirects to server
* Server validates request
* Server redirects to google
* Google does auth things
* Google redirects to Server
* Server redirects to Browser App

## Todo list
* Collect default settings (such as token expiry) into one set of constants.
* Add a Database configuration component to SystemConfiguration.
* Add a configuration API that allows us to modify server configuration.
* Add an initial first-time load context listener that sets up data for this 
  application.
* Add API endpoints for data management- user, identities, clients, 
  authenticators, etc.
* Implement generic secondary OpenID authenticator.
* Implement Google Auth authenticator.
* Implement Facebook authenticator.
* Everything Else.
