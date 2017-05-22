import collections
import os
import sys
import urllib2
import warnings
from openpyxl import load_workbook

DOWNLOAD_FILE_NAME = "string-resource.xlsx"
DOWNLOAD_URL = 'https://docs.google.com/spreadsheets/d/1wbXYVmoA5PlRL298Z6V942nYK26cIhnQgPXOCSnj_mg/pub?output=xlsx'

__author__ = 'jsuch2362'


def download(url):
    try:
        response = urllib2.urlopen(url)

        downloadedFilePath = os.path.basename(DOWNLOAD_FILE_NAME)
        with open(downloadedFilePath, "wb") as local_file:
            local_file.write(response.read())

        return downloadedFilePath

    except urllib2.HTTPError, e:
        print "http error : ", e.code
    except urllib2.URLError, e:
        print "URL error : ", e.reason

    return None


def parsing(filePath):
    warnings.simplefilter("ignore")

    workbook = load_workbook(filePath)

    worksheet = workbook["sheet2"]

    dic = {}

    headerRow = worksheet.rows[0]

    for header in headerRow[:(len(headerRow) - 1)]:
        dic[header.value] = {}

    for row in worksheet.rows[1:]:

        keyInfo = row[len(row) - 1].value.replace("=", "").split("!")
        key = workbook[keyInfo[0]][keyInfo[1]].value

        for cell in row[:(len(row) - 1)]:
            cellInfo = cell.value.replace("=", "").split("!")
            value = workbook[cellInfo[0]][cellInfo[1]].value

            if cellInfo[1].startswith("A"):  # en
                dic["en"][key] = value
            elif cellInfo[1].startswith("B"):  # ko
                dic["ko"][key] = value
            elif cellInfo[1].startswith("C"):  # zh-tw
                dic["zh-rTW"][key] = value
            elif cellInfo[1].startswith("D"):  # zh-cn
                dic["zh-rCN"][key] = value
            elif cellInfo[1].startswith("E"):  # jp
                dic["ja"][key] = value

    for langKey in dic:
        dic[langKey] = collections.OrderedDict(sorted(dic[langKey].items()))

    return dic


def makeXml(dic, absResourcePath):
    xmlDict = {}

    for languageKey in dic:

        if (languageKey == "en"):
            dirPath = absResourcePath + "/values"
        else:
            dirPath = absResourcePath + "/values-" + languageKey

        if not os.path.exists(dirPath):
            os.mkdir(dirPath, 0755)

        f = open(dirPath + "/strings.xml", "wb")
        output = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        output += "\n<resources>"
        for resourceKey in dic[languageKey]:
            if resourceKey is None:
                continue
            if dic[languageKey][resourceKey] is None:
                dic[languageKey][resourceKey] = ""
            output += "\n"
            output += "\t<string name=\"%s\">\"%s\"</string>" % (
                resourceKey, dic[languageKey][resourceKey])

        output += "\n</resources>"

        f.write(output.encode('utf-8'))
        f.close()

    return xmlDict


def main():
    if len(sys.argv) <= 1:
        print "Please Set Argv Resource Path"
        return

    resourcePath = sys.argv[1]
    absResourcePath = os.path.abspath(resourcePath)

    if not os.path.exists(absResourcePath):
        print "Invalide Resource Path : " + absResourcePath
        return

    downloadedFilePath = download(DOWNLOAD_URL)
    if downloadedFilePath:
        dic = parsing(downloadedFilePath)

    if dic:
        makeXml(dic, absResourcePath)

    if downloadedFilePath:
        os.remove(downloadedFilePath)


if __name__ == '__main__':
    main()
