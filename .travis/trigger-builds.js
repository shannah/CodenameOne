var Travis = require('travis-ci');
(function() {
  if (!process.env.GH_TOKEN) {
    console.log("Skipping trigger-builds because GH_TOKEN environment variable is not set");
    return;
  }
  if (!process.env.CN1_TRIGGER_REPOS) {
    console.log("Skipping trigger-builds because CN1_TRIGGER_REPOS environment variable is not set");
    return;
  }
  var repos = process.env.CN1_TRIGGER_REPOS.split(' ');


  var travis = new Travis({
  	version: '2.0.0'
  });

  travis.authenticate({

  	// available through Travis CI
  	// see: http://kamranicus.com/blog/2015/02/26/continuous-deployment-with-travis-ci/
  	github_token: process.env.GH_TOKEN

  }, function (err, res) {
  	if (err) {
  		return console.error(err);
  	}
    repos.forEach(function(repo) {
    	travis.repos(repo.split('/')[0], repo.split('/')[1]).builds.get(function (err, res) {
    		if (err) {
    			return console.error(err);
    		}

    		travis.requests.post({
    			build_id: res.builds[0].id
    		}, function (err, res) {
    			if (err) {
    				return console.error(err);
    			}
    			console.log(res.flash[0].notice);
    		});
    	});
    });
  });
})();
