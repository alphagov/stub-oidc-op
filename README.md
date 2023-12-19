# stub-oidc-op 

> **Verify has Closed**
>
>This repository is out of date and has been archived

Stub OIDC OP is a very simple stub implementation of an OpenID Connect Provider currently using the Hybrid flow. There is currently no Trust Infrastructure in this implementation and it is very much a work in progress.  

Stub OIDC OP uses Redis to store the authentication code, access token and ID token.

You can find the Stub OpenID Client [here](https://github.com/alphagov/stub-oidc-broker)
You can find the Stub Trust framework RP [here](https://github.com/alphagov/stub-trustframework-rp)

### Stub OIDC OP can currently peforms 3 main functions 
* Receives an Authentication Request from a Client and return back an Authentication Code, ID Token and Access Token so the client can perform validation as per the Open ID Connect Spec.  
* Receive an Authentication Code and return an Access Token and ID Token
* Receive an Access Token and return user detail to the stub client

### To use stub-oidc-op
* Ensure you have [Stub OIDC OP](https://github.com/alphagov/stub-oidc-broker) and [Stub OIDC Broker](https://github.com/alphagov/stub-trustframework-rp) up and running
* Run startup.sh
* Go to http://localhost:4410/ in your browser and click Send request

### Stub OIDC OP runs on the PAAS 
* To deploy Stub OIDC OP simply login to the PAAS and select the build-learn space. 
* Run './gradlew pushToPaas' and this will deploy the app.

### For more information about Open ID Connect - 
* Open ID Connect Spec - https://openid.net/specs/openid-connect-core-1_0.html
* Diagrams of all the OpenID Connect flows - https://medium.com/@darutk/diagrams-of-all-the-openid-connect-flows-6968e3990660
* Dev overflow of OpenID Connect - https://developers.onelogin.com/openid-connect

## License

[MIT](https://github.com/alphagov/stub-oidc-op/blob/master/LICENCE)
