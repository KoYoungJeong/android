import json
import urllib
import urllib2

__author__ = 'jsuch2362'

AUTH_URL = "http://192.168.1.202:5000//webapi/auth.cgi"
ACCOUNT = 'tee'
PASSWORD = '1234qwer'


def getSessionId():
    authParams = {}

    authParams['api'] = 'SYNO.API.Auth'
    authParams['version'] = '4'
    authParams['method'] = 'login'
    authParams['account'] = ACCOUNT
    authParams['passwd'] = PASSWORD
    authParams['session'] = 'FileStation'
    authParams['format'] = 'sid'

    authurl = AUTH_URL + '?' + urllib.urlencode(authParams);
    response = urllib2.urlopen(authurl)
    data = json.load(response)
    return data['data']['sid']
