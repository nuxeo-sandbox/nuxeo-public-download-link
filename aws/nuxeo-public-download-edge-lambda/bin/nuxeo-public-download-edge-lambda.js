#!/usr/bin/env node

const cdk = require('@aws-cdk/core');
const { NuxeoPublicDownloadEdgeLambdaStack } = require('../lib/nuxeo-public-download-edge-lambda-stack');

const app = new cdk.App();
new NuxeoPublicDownloadEdgeLambdaStack(app, 'NuxeoPublicDownloadEdgeLambdaStack');
