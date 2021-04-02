# Description

A plugin to generate public download URLs for files stored in the Nuxeo Platform without re-inventing the wheel 

# How to build
```
git clone https://github.com/nuxeo-sandbox/nuxeo-public-download-link
cd nuxeo-public-download-link
mvn clean install
```

# Plugin Features

## Relies on existing features
This plugin relies on the platform download service and permission framework. 

Some benefits of this approach are:
* there is no escalation of privileges to process the download request.
* [file download permissions](https://doc.nuxeo.com/nxdoc/file-download-security-policies/) are still enforced
* download links can be limited in time and easily revoked
* all the existing download features are available: byte range, S3 direct download, Cloudfront integration...

## Permissions
Public links contains a token which is used as the ID of a READ permission ACL on the target Document.
The ACL is set for a transient user (not a real user) which name is "transient/" + token.

## A public download servlet
The servlet is not behind the platform authentication filter and does not require authentication. 
Obviously it does check the validity of download requests using the token in the URL.

## Automation Operations
Two Automation operations provides an API to generate and revoke public download links. This can be leveraged in webui for example.

Create a download link
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

Revoke a download link
```
curl --location --request POST 'http://localhost:8080/nuxeo/site/api/v1/automation/RevokePublicDownloadLink' \
--header 'Authorization: Basic ...' \
--header 'Content-Type: application/json' \
--data-raw '{
    "input":"doc:/default-domain/workspaces/test/mydoc",
    "params": {
        "xpath":"file:content"
    }
}'
```

## API enricher 
The plugin provides an [API document enricher](https://doc.nuxeo.com/nxdoc/content-enrichers/) to fetch all the existing download links on a document

Example:
```
curl --location --request GET 'http://localhost:8080/nuxeo/api/v1/path/default-domain/workspaces/mydoc' \
--header 'Authorization: Basic ...' \
--header 'Content-Type: application/json' \
--header 'enrichers-document: publicDownload'
```

## User Interface

The plugin contains a webui contribution which adds a document action. The action is available for all non folderish documents by default.

![UI action screenshot](https://github.com/nuxeo-sandbox/nuxeo-public-download-link/blob/master/documentation/screenshot_action.png)

The action opens a dialog from where users can create and revoke public download links

![UI Dialog screenshot](https://github.com/nuxeo-sandbox/nuxeo-public-download-link/blob/master/documentation/screenshot_dialog.png)

## Cloudfront and Edge Lambda support

When cloudfront integration is enabled, this plugin can generate a cloudfront url. An edge lambda function is used to validate the link by calling the Nuxeo Application on the [origin request event](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/lambda-cloudfront-trigger-events.html).
By using this particular event, the lambda function is called only if the target file is not already cached in cloudfront.

Setup information at available in [there](https://github.com/nuxeo-sandbox/nuxeo-public-download-link/blob/master/aws/nuxeo-public-download-edge-lambda/README.md)

Edit nuxeo.conf and add the following property:

```
org.nuxeo.labs.download.link.service.cloudfront.enable=true
```

# Known limitations
* The permission start and end dates are not supported yet.
* AWS Cloudfront invalidation request when a link is revoked not yet implemented.

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# Nuxeo Marketplace
This plugin is published on the [marketplace](https://connect.nuxeo.com/nuxeo/site/marketplace/package/nuxeo-public-download-link)

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).
