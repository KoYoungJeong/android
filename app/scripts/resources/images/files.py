import json
import os
import re
import shutil
import urllib
import urllib2
import zipfile

__author__ = 'jsuch2362'

LIST_URL = 'http://192.168.1.202:5000//webapi/entry.cgi'
DOWNLOAD_URL = 'http://192.168.1.202:5000//webapi/entry.cgi'
DRAWABLE_PATH = 'drawable-xxhdpi'


def getDownloadPath(sid):
    params = {}
    params['api'] = 'SYNO.FileStation.List'
    params['version'] = '1'
    params['method'] = 'list_share'
    params['_sid'] = sid

    rootPathUrl = LIST_URL + '?' + urllib.urlencode(params)
    response = urllib2.urlopen(rootPathUrl)
    rootData = json.load(response)
    rootPath = rootData['data']['shares'][0]['path']

    pathParams = {}
    pathParams['api'] = 'SYNO.FileStation.List'
    pathParams['version'] = '1'
    pathParams['method'] = 'list'
    pathParams['_sid'] = sid
    pathParams['folder_path'] = rootPath

    filePathUrl = LIST_URL + '?' + urllib.urlencode(pathParams)
    filePathUrl = filePathUrl.replace('+', '%20')
    fileData = json.load(urllib2.urlopen(filePathUrl))
    return fileData['data']['files'][0]['path']


def download(sid, downloadFilePath):
    params = {}

    params['api'] = 'SYNO.FileStation.Download'
    params['version'] = '1'
    params['_sid'] = sid
    params['method'] = 'download'
    params['path'] = downloadFilePath
    params['mode'] = 'open'

    rawData = urllib.urlencode(params)
    rawData = rawData.replace('+', '%20')
    downloadUrl = DOWNLOAD_URL + '?' + rawData
    response = urllib2.urlopen(downloadUrl)

    local_file = open(os.path.basename('files.zip'), 'wb')
    local_file.write(response.read())
    local_file.close()

    return local_file.name


def unarchive(file, absResourcePath):
    drawablePath = absResourcePath + '/' + DRAWABLE_PATH;
    if os.path.exists(drawablePath):
        shutil.rmtree(drawablePath)

    os.makedirs(drawablePath)
    zips = zipfile.ZipFile(open(file, 'rb'))
    for file in zips.filelist:
        files = file.filename.split('/')
        fileName = files[len(files) - 1]
        if len(fileName) == 0:
            continue
        elif fileName == 'Thumbs.db':
            continue
        elif re.compile("^[a-z][a-zA-Z0-9_]+(\.png|\.9.png)$").match(fileName) is None:
            continue
        output = open(drawablePath + '/' + fileName, 'wb')
        output.write(zips.read(file.filename))
        output.close()

    zips.close()


def delete(file):
    os.remove(file)
