## Description

A plugin to generate public download URLs for files stored in the Nuxeo Platform without re-inventing the wheel 

## How to build
```
git clone https://github.com/nuxeo-sandbox/nuxeo-public-download-link
cd pclm
mvn clean install
```

## Plugin Features

### Relies on existing features
This plugin relies on the platform download service and permission framework. 

Some of the benefits of this approach are:
* there is no escalation of privileges to process the download request.
* download links can be limited in time and easily revoked
* all the existing download features are available: byte range, S3 direct download, Cloudfront integration... 

### Permissions
Public links contains a token which is used as the ID of a READ permission ACL on the target Document.
The ACL is set for a transient user (not a real user) which name is "transient/" + token.

### A public download servlet
The servlet is not behind the platform authentication filter and does not require authentication. 
Obviously it does check the validity of download requests using the token in the URL.

### An Automation Operation
Two Automation operations provides an API to generate and revoke public download links. This can be leveraged in webui for example.

Example:
```
curl --location --request POST 'http://localhost:8080/nuxeo/site/api/v1/automation/CreatePublicDownloadLink' \
--header 'Authorization: Basic ...' \
--header 'Content-Type: application/json' \
--data-raw '{
    "input":"doc:/default-domain/workspaces/test/mydoc",
    "params": {
        "xpath":"file:content"
    }
}'
```

## Known limitations
The permission start and end dates are not supported yet.

## About Nuxeo
[Nuxeo](https://www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.

Learn more at www.nuxeo.com.
