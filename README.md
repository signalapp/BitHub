BitHub
=================

BitHub is a service that will automatically pay a percentage of Bitcoin funds for every submission to a GitHub repository.

Building
-------------

    $ git clone https://github.com/WhisperSystems/BitHub.git
    $ cd BitHub
    $ mvn3 package

Running
-----------

1. Create a GitHub account for your BitHub server.
1. Create a Coinbase account for your BitHub server.
1. Add the above credentials to `config/sample.yml`
1. Execute `$ java -jar target/BitHub-0.1.jar server config/yourconfig.yml`

Deploying To Heroku
------------

1. `$ heroku create your_app_name`
1. `$ heroku config:set GITHUB_USER=your_bithub_username`
1. `$ heroku config:set GITHUB_TOKEN=your_bithub_authtoken`
1. `$ heroku config:set GITHUB_REPOSITORIES="[\"https://github.com/youraccount/yourrepo\", \"https://github.com/youraccount/yourotherrepo\"]"`
1. `$ heroku config:set COINBASE_API_KEY=your_api_key`
1. `$ git remote add your_heroku_remote`
1. `$ git push heroku master`


Mailing list
------------

Have a question? Ask on our mailing list!

whispersystems@lists.riseup.net

https://lists.riseup.net/www/info/whispersystems

Current BitHub Payment For Commit: 
=================
![Current Price](https://bithub.herokuapp.com/v1/status/payment/commit)

