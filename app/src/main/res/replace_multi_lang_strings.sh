#!/bin/bash

# 다국어 언어 지원을 위해 /values-xx/strings.xml 에는 각 언어에 해당하는 문자열 리소스가 존재한다.
# JANDI 를 위해 사용하는 POEditor 에서 작업한 결과를 export 하면 다음의 문제가 생기기 때문에
# 이를 보완하는 스크립트가 필요하다.
#
# <string ...>hello world</string>  =>  <string ...>"hello world"</string>
# <string ...>&#xa0;hello world</string>  =>  <string ...>" hello world"</string>

for file in `ls ./values*/strings.xml`
do
    if [ -f $file ]
    then
        tmpfile="strings_tmp.xml"
        echo "$file has been changed"
        # <string></string>에 있는 " 제거.
        sed -e 's/>"/>/g' $file | sed -e 's/"</</g' > $tmpfile
        # <string> 바로 뒤, </string> 바로 앞 공백을 &#xa0;로 변경.
        sed 's/"> /\">\&#xa0;/g' $tmpfile | sed 's/ <\//\&#xa0;<\//g' > $file
        rm $tmpfile
    fi
done