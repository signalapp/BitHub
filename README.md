BitHub
=================

[![Build Status](https://travis-ci.org/WhisperSystems/BitHub.png?branch=master)](https://travis-ci.org/WhisperSystems/BitHub)

BitHub is a service that will automatically pay a percentage of Bitcoin funds for every submission to a GitHub repository.

More information can be found in our [announcement blog post](https://whispersystems.org/blog/bithub).

Opting Out
----------

If you'd like to opt out of receiving a payment, simply include the string "FREEBIE" somewhere in your commit message, and you will not recieve BTC for that commit.


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

```
$ heroku create your_app_name
$ heroku config:set GITHUB_USER=your_bithub_username
$ heroku config:set GITHUB_TOKEN=your_bithub_authtoken
$ heroku config:set GITHUB_REPOSITORIES="[\"https://github.com/youraccount/yourrepo\", \"https://github.com/youraccount/yourotherrepo\"]"
$ heroku config:set COINBASE_API_KEY=your_api_key
$ git remote add your_heroku_remote
$ git push heroku master
```

Mailing list
------------

Have a question? Ask on our mailing list!

whispersystems@lists.riseup.net

https://lists.riseup.net/www/info/whispersystems

Current BitHub Payment For Commit: 
=================
![Current Price](https://bithub.herokuapp.com/v1/status/payment/commit)

