Regex RequestR
==============

A tool written in Java (UI built with JavaFX) to make testing and _developing_ of regular expressions for Java (and any other compatible implementation of Regex) less a pain.  
Tools like [RegexR](https://regexr.com) and [Regex101](https://regex101.com/) are fine, but due to JavaScript's limitations and the slightly different implementation of regex in Java, expressions written with the help those tools sometimes did not compile.  
Another motivation for this tool was the need to filter the results of HTTP requests without copy-pasting.  
The application uses [OkHttp from Square](http://square.github.io/okhttp) to do HTTP requests.  

Download
--------

Either check out the _dist_ folder - the latest precompiled stable jar of the application can be found there, or clone, customize and build the application yourself.

Improvements & Stuff todo
-------------------------
- Add highlighting of matches 
- Advanced testing

Contribution
------------

Feel free to open pull requests for changes or fixes.  
[Codestyle / Formatting (Eclipse formatter)](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml)

[License](../master/license.md)
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
