var lock = new Auth0Lock('Qs4Ep1x2dZDZjiRuaZfys2JqEkRf9XVD', 'dwmartin41.eu.auth0.com');

var hash = lock.parseHash(window.location.hash);

if(hash){
    if (hash.error) {
	console.log("There was an error logging in", hash.error);
	alert('There was an error: ' + hash.error + '\n' + hash.error_description);
    } else {
	window.location.href = '/token/' + hash.id_token;
    }
}


lock.show({closable: false});
