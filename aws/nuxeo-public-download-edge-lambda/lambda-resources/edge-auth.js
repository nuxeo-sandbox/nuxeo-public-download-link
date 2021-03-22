'use strict';

const https = require('https');
const url = require('url');

const publicPath = '/public/';
const nuxeoDomainHeader = 'nx-domain';
const tokenQueryString = 'download_token';

exports.handler = (event, context, callback) => {
    const request = event.Records[0].cf.request;
    console.log('Original request: '+JSON.stringify(request));

    const uri = request.uri;

    const nuxeoHostName = request.origin.s3.customHeaders[nuxeoDomainHeader][0].value;
    console.log('Nuxeo Domain: '+nuxeoHostName);

    if (uri.startsWith(publicPath)) {
        let segments = uri.substring(publicPath.length).split('/');

        const token = segments[0];

        // remove public prefix
        segments.shift();

        //rebuild path
        const nuxeoPath = segments.join('/');

        const nuxeoUrl = `/nuxeo/${nuxeoPath}?${tokenQueryString}=${token}`;
        console.log('Nuxeo URL: '+ nuxeoUrl);

        const options = {
            hostname: nuxeoHostName,
            port: 443,
            path: nuxeoUrl,
            method: 'HEAD'
        }

        https.get(options, (res) => {
            console.log('Nuxeo Response Code: '+res.statusCode);
            if (res.statusCode !== 302) {
                callback(Error('Not Found'));
            } else {
                request.uri = url.parse(res.headers.location).pathname;
                console.log('Modified request: '+JSON.stringify(request));
                callback(null, request);
            }
        }).on('error', (e) => {
            console.log('Nuxeo Error '+e);
            callback(Error(e))
        })
    } else {
        callback(null, request);
    }
};
