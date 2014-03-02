BitHub
=================

[![Build Status](https://travis-ci.org/WhisperSystems/BitHub.png?branch=master)](https://travis-ci.org/WhisperSystems/BitHub)

BitHub is a service that will automatically pay a percentage of Bitcoin funds for every submission to a GitHub repository.

More information can be found in our [announcement blog post](https://whispersystems.org/blog/bithub).

As a user
=========

Claiming your share of the bitcoin reward when sending code to projects using BitHub is quite simple: if a
[Coinbase](https://coinbase.com) account with the same email address you used to commit your changes exists, it will be credited
the bitcoin amount automatically. If no such account exists, Coinbase will send you an email message with further instructions
on how to retrieve your bounty.

Technically, the project's BitHub server will query the Coinbase API for a bitcoin address using your commit's email address, and
use that to make a payment on a Coinbase account.

That's it!

Opting Out
----------

If you'd like to opt out of receiving a payment, simply include the string "FREEBIE" somewhere in your commit message, and you will not receive BTC for that commit.

Using BitHub on your projects
=============================

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
$ heroku config:set GITHUB_WEBHOOK_PASSWORD=your_webhook_password
$ heroku config:set GITHUB_REPOSITORIES="[{\"url\" : \"https://github.com/youraccount/yourrepo\"}, {\"url\" : \"https://github.com/youraccount/yourotherrepo\"}]"
$ heroku config:set COINBASE_API_KEY=your_api_key
$ git remote add your_heroku_remote
$ git push heroku master
```

Mailing list
============

Have a question? Ask on our mailing list!

whispersystems@lists.riseup.net

https://lists.riseup.net/www/info/whispersystems

Current BitHub Payment For Commit: 
=================
![Current Price](https://bithub.herokuapp.com/v1/status/payment/commit)

