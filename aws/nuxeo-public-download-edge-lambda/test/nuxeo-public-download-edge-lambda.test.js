const { expect, matchTemplate, MatchStyle } = require('@aws-cdk/assert');
const cdk = require('@aws-cdk/core');
const NuxeoPublicDownloadEdgeLambda = require('../lib/nuxeo-public-download-edge-lambda-stack');

test('Empty Stack', () => {
    const app = new cdk.App();
    // WHEN
    const stack = new NuxeoPublicDownloadEdgeLambda.NuxeoPublicDownloadEdgeLambdaStack(app, 'MyTestStack');
    // THEN
    expect(stack).to(matchTemplate({
      "Resources": {}
    }, MatchStyle.EXACT))
});
