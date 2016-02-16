import os
import sys
import files
import session


def main():
    if len(sys.argv) <= 1:
        print "Please Set Argv Resource Path"
        return

    resourcePath = sys.argv[1]
    absResourcePath = os.path.abspath(resourcePath)
    sid = session.getSessionId()
    downloadFilePath = files.getDownloadPath(sid)
    zipFile = files.download(sid, downloadFilePath)
    files.unarchive(zipFile, absResourcePath)
    files.delete(zipFile)

if __name__ == '__main__':
    main()
