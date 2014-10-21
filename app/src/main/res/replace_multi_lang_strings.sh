#!/bin/bash

# 다국어 언어 지원을 위해 /values-xx/strings.xml 에는 각 언어에 해당하는 문자열 리소스가 존재한다.
# JANDI 를 위해 사용하는 POEditor 에서 작업한 결과를 export 하면 다음의 문제가 생기기 때문에
# 이를 보완하는 스크립트가 필요하다.
#
# <string ...>hello world</string>  =>  <string ...>"hello world"</string>
# <string ...>&#xa0;hello world</string>  =>  <string ...>" hello world"</string>
# <string ...>hello\nworld</string>  =>  <string ...>hello
# world</string>
for dir in `ls -d ./values*`
do
    file="$dir/strings.xml"
    if [ -f $file ]
    then
        tmpfile="$dir/strings_out.xml"
        echo "$file ==> $tmpfile"
        sed -e 's/>" />&#xa0;/g' $file | sed -e 's/>"/>/g' | sed -e 's/ "</&#xa0;</g' | sed -e 's/"</</g' > $tmpfile
    fi
done