## Phonsole-CLI [![npm version](https://badge.fury.io/js/phonsole.svg)](https://badge.fury.io/js/phonsole)

This is the CLI for the phonsole application. To install it, run `npm install -g phonsole`. 

### How to use

The app listens to the standard input, and sends it to the phonsole server. For example, if you want to send the output of the `script.sh` script you pipe the output of the script to phonsole like so:

`./script.sh --color 2>&1 | phonsole`

Phonsole can only read the stdout stream from `script.sh`, so we have to use `2>&1` to redirect stderr to stdout. Also, many scripts will not use colours if they detect that the output is not a terminal, and you have to force them to use colours by passing a `--color` flag or similar.

### Options

By default, phonsole will use https://phonsole-server.herokuapp.com as the server url. You can use the `PHONSOLE_SERVER` environment variable to override this.

Command line args:

```bash
	-v, --verbose     Turn on verbose output
	--id              The id of the console, shown in the web UI. Defaults to a random name.
```

## License

Distributed under the GPL V3 license
